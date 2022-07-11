package com.tarantula.platform;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Lobby;
import com.icodesoftware.Statistics;
import com.icodesoftware.service.MetricsListener;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.statistics.StatisticsIndex;

import java.io.IOException;

public class GameCluster extends OnApplicationHeader implements Portable , Configurable, MetricsListener {

    public final static String GAME_CLUSTER_CONFIGURATION_TYPE = "GameCluster";

    public final static String NAME="1";
    public final static String MODE="2";//pve | pvp |tvp|tvt
    public final static String GAME_LOBBY = "3";
    public final static String GAME_SERVICE = "4";
    public final static String GAME_DATA = "5";
    public final static String OWNER = "6";
    public final static String ACCESS_KEY = "7";
    public final static String TIMESTAMP = "8";
    public final static String DISABLED = "9";
    public final static String PUBLISHING_ID = "10";
    public final static String TOURNAMENT_ENABLED = "11";
    public final static String LOBBY_PRE_SETUP_NAME ="12";

    public Lobby gameLobby;
    public Lobby serviceLobby;
    public Lobby dataLobby;

    public Statistics statistics;

    public final static String TOURNAMENT_LOOKUP_INDEX = "tournament";

    public final static String TOURNAMENT_SCHEDULE_LOOKUP_INDEX = "schedule";
    public final static String PLAY_LIST_INDEX ="playlist";

    public final static String GAME_ASSET_CATEGORY_TEMPLATE = "assets";
    public final static String GAME_COMPONENT_CATEGORY_TEMPLATE = "components";
    public final static String GAME_COMMODITY_CATEGORY_TEMPLATE = "commodities";
    public final static String GAME_ITEM_CATEGORY_TEMPLATE = "items";
    public final static String GAME_APPLICATION_CATEGORY_TEMPLATE = "applications";

    public final static String GAME_COMMON_TYPE_TEMPLATE = "common-type-settings";

    @Override
    public int getClassId() {
       return PortableEventRegistry.GAME_CLUSTER_CID;
    }

    @Override
    public void writePortable(PortableWriter portableWriter) throws IOException {
        portableWriter.writeBoolean("1",successful);
        portableWriter.writeUTF("2",message);
        if(successful){
            portableWriter.writeUTF("3",this.bucket);
            portableWriter.writeUTF("4",oid);
        }
    }

    @Override
    public void readPortable(PortableReader portableReader) throws IOException {
        successful = portableReader.readBoolean("1");
        message = portableReader.readUTF("2");
        if(successful){
            bucket = portableReader.readUTF("3");
            oid = portableReader.readUTF("4");
        }
    }
    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("successful",successful);
        jo.addProperty("message",message);
        jo.addProperty("gameClusterId",distributionKey());
        jo.addProperty("name",(String)property(GameCluster.NAME));
        jo.addProperty("mode",(String)property(GameCluster.MODE));
        jo.addProperty("setup",(String)property(GameCluster.LOBBY_PRE_SETUP_NAME));
        jo.addProperty("gameLobby",(String)property(GameCluster.GAME_LOBBY));
        jo.addProperty("gameService",(String)property(GameCluster.GAME_SERVICE));
        jo.addProperty("gameData",(String)property(GameCluster.GAME_DATA));
        jo.addProperty("accessKey",(String)property(GameCluster.ACCESS_KEY));
        jo.addProperty("tournamentEnabled",(Boolean)property(GameCluster.TOURNAMENT_ENABLED));
        jo.addProperty("disabled",(Boolean)property(GameCluster.DISABLED));
        return jo;
    }
    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    public Descriptor serviceWithCategory(String category){
        if(serviceLobby==null){
            return null;
        }
        Descriptor loaded =null;
        for(Descriptor d : serviceLobby.entryList()){
            if(d.category().equals(category)){
                loaded = d;
                break;
            }
        }
        if(loaded!=null) return loaded;
        for(Descriptor d : dataLobby.entryList()){
            if(d.category().equals(category)){
                loaded = d;
                break;
            }
        }
        return loaded;
    }
    public Descriptor gameWithKey(String key){
        if(gameLobby==null){
            return null;
        }
        Descriptor loaded =null;
        for(Descriptor d : gameLobby.entryList()){
            if(d.distributionKey().equals(key)){
                loaded = d;
                break;
            }
        }
        return loaded;
    }

    @Override
    public void onUpdated(String s, double v) {
        if(statistics==null) setup();
        statistics.entry(s).update(v).update();
    }

    public <T extends Configurable> T setup(){
        StatisticsIndex statisticsIndex = new StatisticsIndex();
        statisticsIndex.distributionKey(this.distributionKey());
        statisticsIndex.dataStore(this.dataStore);
        this.dataStore.createIfAbsent(statisticsIndex,true);
        this.statistics = statisticsIndex;
        return null;
    }

}
