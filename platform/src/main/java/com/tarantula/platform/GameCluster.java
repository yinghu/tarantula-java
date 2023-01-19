package com.tarantula.platform;

import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.Configurable;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Lobby;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.event.PortableEventRegistry;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.util.SystemUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class GameCluster extends OnApplicationHeader implements Portable , Configurable {

    public final static String GAME_CLUSTER_CONFIGURATION_TYPE = "GameCluster";

    public final static String NAME="1";
    public final static String MODE="2";//pve | pvp |tvp|tvt
    public final static String GAME_LOBBY = "3";
    public final static String GAME_SERVICE = "4";
    public final static String GAME_DATA = "5";
    public final static String OWNER = "6";
    //public final static String ACCESS_KEY = "7";
    //public final static String TIMESTAMP = "8";
    public final static String DISABLED = "9";
    public final static String PUBLISHING_ID = "10";
    public final static String TOURNAMENT_ENABLED = "11";
    public final static String LOBBY_PRE_SETUP_NAME ="12";
    public final static String DEDICATED ="13";
    public final static String GAME_ICON = "14";
    public final static String DEVELOPER_ICON = "15";
    public final static String DEVELOPER = "16";
    public Lobby gameLobby;
    public Lobby serviceLobby;
    public Lobby dataLobby;



    public final static String TOURNAMENT_LOOKUP_INDEX = "tournament";

    public final static String TOURNAMENT_SCHEDULE_LOOKUP_INDEX = "schedule";
    public final static String PLAY_LIST_INDEX ="playlist";

    public final static String GAME_ASSET_CATEGORY_TEMPLATE = "assets";
    public final static String GAME_COMPONENT_CATEGORY_TEMPLATE = "components";
    public final static String GAME_COMMODITY_CATEGORY_TEMPLATE = "commodities";
    public final static String GAME_ITEM_CATEGORY_TEMPLATE = "items";
    public final static String GAME_APPLICATION_CATEGORY_TEMPLATE = "applications";

    public final static String GAME_COMMON_TYPE_TEMPLATE = "common-type-settings";

    public final static String GAME_UPGRADE_CATEGORY_TEMPLATE = "upgrade";


    protected ServiceContext serviceContext;

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
        jo.addProperty("developer",(String)property(GameCluster.DEVELOPER));
        jo.addProperty("gameIcon",(String)property(GameCluster.GAME_ICON));
        jo.addProperty("developerIcon",(String)property(GameCluster.DEVELOPER_ICON));
        jo.addProperty("tournamentEnabled",(Boolean)property(GameCluster.TOURNAMENT_ENABLED));
        jo.addProperty("dedicated",(Boolean)property(GameCluster.DEDICATED));
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

    public void setup(ServiceContext serviceContext){
        try{
            this.serviceContext = serviceContext;
            String deployDir = serviceContext.node().deployDirectory();
            //GameCluster gameCluster = this;//configuration;
            Path _config_game = Paths.get(deployDir+"/conf/"+this.property(GameCluster.NAME));
            if(!Files.exists(_config_game)){
                Path _config_assets = Paths.get(_config_game.toString(),"assets");
                Path _config_components = Paths.get(_config_game.toString(),"components");
                Path _config_commodities = Paths.get(_config_game.toString(),"commodities");
                Path _config_items = Paths.get(_config_game.toString(),"items");
                Path _config_applications = Paths.get(_config_game.toString(),"applications");
                Files.createDirectories(_config_assets);
                Files.createDirectories(_config_components);
                Files.createDirectories(_config_commodities);
                Files.createDirectories(_config_items);
                Files.createDirectories(_config_applications);
                URL url = Thread.currentThread().getContextClassLoader().getResource("config-template");
                copyTemplateFile(url,_config_game);

                URL srcAssets = Thread.currentThread().getContextClassLoader().getResource("config-template/assets");
                copyTemplateFile(srcAssets,_config_assets);

                URL srcComponents = Thread.currentThread().getContextClassLoader().getResource("config-template/components");
                copyTemplateFile(srcComponents,_config_components);

                URL srcCommodities = Thread.currentThread().getContextClassLoader().getResource("config-template/commodities");
                copyTemplateFile(srcCommodities,_config_commodities);

                URL srcItems = Thread.currentThread().getContextClassLoader().getResource("config-template/items");
                copyTemplateFile(srcItems,_config_items);

                URL srcApplications = Thread.currentThread().getContextClassLoader().getResource("config-template/applications");
                copyTemplateFile(srcApplications,_config_applications);
            }
            Path _web_game = Paths.get(deployDir+"/web/"+this.property(GameCluster.NAME));
            if(!Files.exists(_web_game)){
                Files.createDirectories(_web_game);
            }
        }catch (Exception ex){
            //log.error("error on game cluster->"+configuration.property(GameCluster.NAME),ex);
            throw new RuntimeException(ex);
        }
    }
    private void copyTemplateFile(URL src,Path dest) throws Exception{
        String[] fs = new File(src.getFile()).list((m, n)-> n.endsWith(".json"));
        String _path = src.getFile();
        int wp = _path.indexOf(":");
        if(wp>0){
            _path = _path.substring(wp+1);
        }
        for(String f : fs){
            Path _item = Paths.get(_path+"/"+f);
            Path _game = Paths.get(dest+"/"+f);
            Files.copy(_item,_game, StandardCopyOption.COPY_ATTRIBUTES);
        }
    }

    public ApplicationPreSetup applicationPreSetup(){
        ApplicationPreSetup applicationPreSetup = SystemUtil.applicationPreSetup((String) properties.get(GameCluster.LOBBY_PRE_SETUP_NAME));
        applicationPreSetup.setup(serviceContext);
        return applicationPreSetup;
    }
}
