package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.*;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.util.DescriptorSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.util.HashMap;
import java.util.Map;

public class GameLobbyAdminModule implements Module {
    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    private int maxGameLobbyCount;
    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if (session.action().equals("onGameLobbyList")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            session.write(toJson(gameCluster.gameLobby).toString().getBytes());
        }
        else if(session.action().equals("onGameServiceList")){
            GameServiceContext gsc = new GameServiceContext();
            GameCluster gc = this.deploymentServiceProvider.gameCluster(session.name());
            if(gc!=null){
                gsc.lobby= gc.serviceLobby;
                session.write(gsc.toJson().toString().getBytes());
            }else{
                session.write(JsonUtil.toSimpleResponse(false,"no game cluster for key ["+session.name()+"]").getBytes());
            }
        }
        else if(session.action().equals("onGameDataList")){
            session.write(JsonUtil.toSimpleResponse(false,"").getBytes());
        }
        else if(session.action().equals("__onGameDataList")){
            GameClusterDataStoreContext gsc = new GameClusterDataStoreContext();
            GameCluster gc = this.deploymentServiceProvider.gameCluster(session.name());
            ApplicationPreSetup applicationPreSetup = SystemUtil.applicationPreSetup((String)gc.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            Lobby lobby = gc.dataLobby;
            gsc.name = lobby.descriptor().typeId();
            gsc.tag = lobby.entryList().get(0).tag();
            //_data
            DataStore ds = applicationPreSetup.dataStore(context,gc,"player");
            gsc.gameDataStoreList.add(new GameClusterDataStoreContext.GameDataStore("player",ds.name(),ds.count()));

            //_service
            DataStore ss = applicationPreSetup.dataStore(context,gc);
            gsc.gameDataStoreList.add(new GameClusterDataStoreContext.GameDataStore("service",ss.name(),ss.count()));

            //_service_room
            DataStore ssr = applicationPreSetup.dataStore(context,gc,"room");
            gsc.gameDataStoreList.add(new GameClusterDataStoreContext.GameDataStore("room",ssr.name(),ssr.count()));

            //_service_configuration
            DataStore cfr = applicationPreSetup.dataStore(context,gc,"configuration");
            gsc.gameDataStoreList.add(new GameClusterDataStoreContext.GameDataStore("configuration",cfr.name(),cfr.count()));

            //_service_tournament
            if((Boolean)gc.property(GameCluster.TOURNAMENT_ENABLED)){
                DataStore sst = applicationPreSetup.dataStore(context,gc,"tournament");
                gsc.gameDataStoreList.add(new GameClusterDataStoreContext.GameDataStore("tournament",sst.name(),sst.count()));
            }

            session.write(gsc.toJson().toString().getBytes());
        }
        else if(session.action().equals("onGameLobby")){
            Map<String,Object> cmd = JsonUtil.toMap(payload);
            String gameClusterId = (String) cmd.get("gameClusterId");
            String applicationId = (String) cmd.get("applicationId");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(gameClusterId);
            Descriptor app = gameCluster.gameWithKey(applicationId);
            GameLobby lobby = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).load(this.context,app);
            session.write(toJson(app,lobby).toString().getBytes());
        }
        else if (session.action().equals("onAddLobby")){
            Map<String,Object> cmd = JsonUtil.toMap(payload);
            String gameClusterId = (String) cmd.get("gameClusterId");
            int lobbyIndex = ((Number) cmd.get("lobbyIndex")).intValue();
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(gameClusterId);
            Lobby lobby = gameCluster.gameLobby;
            if(lobby.entryList().size()<maxGameLobbyCount&&lobbyIndex<=maxGameLobbyCount){
                HashMap<Integer,Descriptor> eMap = new HashMap<>();
                lobby.entryList().forEach((d)->{
                    eMap.put(d.accessRank(),d);
                });
                if(!eMap.containsKey(lobbyIndex)) {
                    Descriptor desc = lobby.entryList().get(0).copy();
                    desc.name("Game Lobby " + lobbyIndex);
                    desc.tag(((String) gameCluster.property(GameCluster.NAME)).toLowerCase() + "/lobby" + lobbyIndex);
                    desc.accessRank(lobbyIndex);
                    String configName = (String) gameCluster.property(GameCluster.MODE);
                    if(this.deploymentServiceProvider.createApplication(desc,(String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME),configName,true)){
                        session.write(JsonUtil.toSimpleResponse(true,"lobby added->"+lobbyIndex).getBytes());
                    }
                    else{
                        session.write(JsonUtil.toSimpleResponse(false,"lobby failed->"+lobbyIndex).getBytes());
                    }
                }else{
                    session.write(JsonUtil.toSimpleResponse(false,"lobby already existed->"+lobbyIndex).getBytes());
                }
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"lobby size is over max count->"+maxGameLobbyCount).getBytes());
            }
        }
        else if(session.action().equals("onDisableLobby")){
            Map<String,Object> cmd = JsonUtil.toMap(payload);
            this.deploymentServiceProvider.disableApplication((String)cmd.get("lobbyId"));
            session.write(payload);
        }
        else if(session.action().equals("onEnableLobby")){
            Map<String,Object> cmd = JsonUtil.toMap(payload);
            this.deploymentServiceProvider.enableApplication((String)cmd.get("lobbyId"));
            session.write(payload);
        }
        else if(session.action().equals("onReloadLobby")){
            Map<String,Object> cmd = JsonUtil.toMap(payload);
            String gameClusterId = (String) cmd.get("gameClusterId");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(gameClusterId);
            String lobbyId = (String)cmd.get("lobbyId");
            Descriptor app = gameCluster.gameWithKey(lobbyId);
            GameLobby lobby = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).load(this.context,app);
            lobby.setup(context);
            lobby.reload();
            //GameZone zone = lobby.list().get(0);
            //this.deploymentServiceProvider.configure(zone.distributionKey());
            session.write(JsonUtil.toSimpleResponse(true,"Lobby reloaded").getBytes());
        }
        else if(session.action().equals("onSaveLobbyZone")){
            String[] keys = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(keys[0]);
            Descriptor app = gameCluster.gameWithKey(keys[1]);
            GameLobby lobby = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).load(this.context,app);
            lobby.setup(context);
            lobby.configureGameZone(payload);
            session.write(JsonUtil.toSimpleResponse(true,"zone updated").getBytes());
        }
        else if(session.action().equals("onSaveLobbyLevel")){
            String[] keys = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(keys[0]);
            Descriptor app = gameCluster.gameWithKey(keys[1]);
            GameLobby lobby = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).load(this.context,app);
            lobby.setup(context);
            lobby.configureArena(payload);
            session.write(JsonUtil.toSimpleResponse(true,"level updated").getBytes());
        }
        else {
            throw new UnsupportedOperationException(session.action()+" not supported");
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext applicationContext) throws Exception {
        this.context = applicationContext;
        this.deploymentServiceProvider = context.serviceProvider(DeploymentServiceProvider.NAME);
        this.maxGameLobbyCount = Integer.parseInt(this.context.configuration("cluster").property("maxGameLobbyCount").toString());
        this.context.log("Game lobby admin module started", OnLog.WARN);
    }

    private JsonObject toJson(Lobby lobby){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        DescriptorSerializer descriptorSerializer = new DescriptorSerializer();
        //jsonObject.add("lobby",descriptorSerializer.serialize(lobby.descriptor(),Descriptor.class,null));
        JsonArray alist = new JsonArray();
        if(lobby!=null){
            lobby.entryList().forEach((a)->{
                alist.add(descriptorSerializer.serialize(a,Descriptor.class,null));
            });
        }
        jsonObject.add("gameLobbyList",alist);
        return jsonObject;
    }
    private JsonObject toJson(Descriptor lobby,GameLobby gameLobby){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        JsonArray zarray = new JsonArray();
        gameLobby.list().forEach((zone)->{
            JsonObject _jo = new JsonObject();
            JsonObject jzon = new JsonObject();
            jzon.addProperty("zoneId",zone.distributionKey());
            jzon.addProperty("name",zone.name()!=null?zone.name():lobby.name());
            jzon.addProperty("levelMatch",zone.levelMatch());
            jzon.addProperty("tag",lobby.tag());
            jzon.addProperty("rank",lobby.accessRank());
            jzon.addProperty("capacity",zone.capacity());
            jzon.addProperty("maxJoinsPerRoom",zone.maxJoinsPerRoom());
            jzon.addProperty("joinsOnStart",zone.joinsOnStart());
            jzon.addProperty("duration",zone.roundDuration()/60000);
            jzon.addProperty("playMode",zone.playMode());
            jzon.addProperty("disabled",lobby.disabled());
            _jo.add("zone",jzon);
            JsonArray jds = new JsonArray();
            for(Arena a: zone.arenas()){
                JsonObject jd = new JsonObject();
                jd.addProperty("arenaId",a.distributionKey());
                jd.addProperty("name",a.name());
                jd.addProperty("level",a.level);
                jd.addProperty("xp",a.xp);
                jd.addProperty("capacity",a.capacity);
                jd.addProperty("joinsOnStart",a.joinsOnStart);
                jd.addProperty("duration",a.duration/60000);
                jd.addProperty("disabled",a.disabled());
                jds.add(jd);
            }
            _jo.add("levels",jds);
            zarray.add(_jo);
        });
        jsonObject.add("list",zarray);
        return jsonObject;
    }
}
