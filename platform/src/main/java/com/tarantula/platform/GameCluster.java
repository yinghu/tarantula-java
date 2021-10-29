package com.tarantula.platform;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Lobby;
import com.tarantula.platform.event.PortableEventRegistry;

import java.io.IOException;

public class GameCluster extends OnApplicationHeader implements Portable {

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

    public final static String TOURNAMENT_LOOKUP_INDEX = "tournament";

    public final static String TOURNAMENT_SCHEDULE_LOOKUP_INDEX = "schedule";
    public final static String PLAY_LIST_INDEX ="playlist";

    public final static String GAME_ASSET_CATEGORY_TEMPLATE = "game-asset-category-settings";
    public final static String GAME_COMPONENT_CATEGORY_TEMPLATE = "game-component-category-settings";
    public final static String GAME_COMMODITY_CATEGORY_TEMPLATE = "game-commodity-category-settings";
    public final static String GAME_ITEM_CATEGORY_TEMPLATE = "game-item-category-settings";
    public final static String GAME_APPLICATION_CATEGORY_TEMPLATE = "game-application-category-settings";

    public final static String PVE_MODE = "pve";
    public final static String PVP_MODE = "pvp";
    public final static String TVE_MODE = "tve";
    public final static String TVT_MODE = "tvt";

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
}
