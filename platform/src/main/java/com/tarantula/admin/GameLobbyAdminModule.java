package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.*;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.util.DescriptorSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.util.HashMap;
import java.util.Map;

public class GameLobbyAdminModule implements Module {
    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    private int maxGameLobbyCount;
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        if (session.action().equals("onGameLobbyList")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            String lobbyTypeId = (String)gameCluster.property(GameCluster.GAME_LOBBY);
            Lobby lobby = this.deploymentServiceProvider.lobby(lobbyTypeId);
            session.write(toJson(lobby).toString().getBytes());
        }
        else if(session.action().equals("onGameServiceList")){
            GameServiceContext gsc = new GameServiceContext();
            GameCluster gc = this.deploymentServiceProvider.gameCluster(session.name());
            gsc.lobby=(this.deploymentServiceProvider.lobby((String) gc.property(GameCluster.GAME_SERVICE)));
            session.write(gsc.toJson().toString().getBytes());
        }
        else if(session.action().equals("onGameDataList")){
            GameDataStoreContext gsc = new GameDataStoreContext();
            GameCluster gc = this.deploymentServiceProvider.gameCluster(session.name());
            Lobby lobby =(this.deploymentServiceProvider.lobby((String) gc.property(GameCluster.GAME_DATA)));
            DataStore ds = this.context.dataStore(lobby.descriptor().typeId().replace("-","_"));
            gsc.name = lobby.descriptor().typeId();
            gsc.tag = lobby.entryList().get(0).tag();
            gsc.dataStore = ds.name();//lobby.descriptor().typeId();
            gsc.dataStoreCount = ds.count();
            DataStore ss = this.context.dataStore(lobby.descriptor().typeId().replace("-data","_service"));
            gsc.serviceStore = ss.name();
            gsc.serviceStoreCount = ss.count();
            session.write(gsc.toJson().toString().getBytes());
        }
        else if(session.action().equals("onGameLobby")){
            Map<String,Object> cmd = JsonUtil.toMap(payload);
            String gameClusterId = (String) cmd.get("gameClusterId");
            String applicationId = (String) cmd.get("applicationId");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(gameClusterId);
            Descriptor app = loadDescriptor(gameCluster,applicationId);
            GameLobby lobby = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).load(this.context,app);
            session.write(toJson(app,lobby.list().get(0)).toString().getBytes());
        }
        else if (session.action().equals("onAddLobby")){
            Map<String,Object> cmd = JsonUtil.toMap(payload);
            String gameClusterId = (String) cmd.get("gameClusterId");
            int lobbyIndex = ((Number) cmd.get("lobbyIndex")).intValue();
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(gameClusterId);
            String lobbyTypeId = (String)gameCluster.property(GameCluster.GAME_LOBBY);
            Lobby lobby = this.deploymentServiceProvider.lobby(lobbyTypeId);
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
                    //desc.index((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
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
            Descriptor app = loadDescriptor(gameCluster,lobbyId);
            GameLobby lobby = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).load(this.context,app);
            GameZone zone = lobby.list().get(0);
            this.deploymentServiceProvider.configure(zone.distributionKey());
            session.write(JsonUtil.toSimpleResponse(true,"Lobby reloaded").getBytes());
        }
        else if(session.action().equals("onSaveLobbyZone")){
            String[] keys = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(keys[0]);
            Descriptor app = loadDescriptor(gameCluster,keys[1]);
            GameLobby lobby = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).load(this.context,app);
            GameZone zone = lobby.list().get(0);
            boolean updated = zone.configureAndValidate(payload);
            if(updated){
                zone.update();
            }
            session.write(JsonUtil.toSimpleResponse(updated,"zone updated ["+updated+"]").getBytes());
        }
        else if(session.action().equals("onSaveLobbyLevel")){
            String[] keys = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(keys[0]);
            Descriptor app = loadDescriptor(gameCluster,keys[1]);
            GameZone zone = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).load(this.context,app);
            boolean[] updated = {false};
            zone.arenas().forEach((a)->{
                if(a.distributionKey().equals(keys[2])){
                    updated[0]= a.configureAndValidate(payload);
                }
            });
            if(updated[0]){
                zone.update();
            }
            session.write(JsonUtil.toSimpleResponse(updated[0],"level updated ["+updated[0]+"]").getBytes());
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
        this.context.log("game lobby admin module started", OnLog.WARN);
    }
    private Descriptor loadDescriptor(GameCluster gameCluster,String lobbyId){
        String lobbyTypeId = (String)gameCluster.property(GameCluster.GAME_LOBBY);
        Lobby lobby = this.deploymentServiceProvider.lobby(lobbyTypeId);
        Descriptor[] descriptors = {null};
        lobby.entryList().forEach((d)->{
            if(d.distributionKey().equals(lobbyId)){
                descriptors[0]=d;
            }
        });
        return descriptors[0];
    }
    private JsonObject toJson(Lobby lobby){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        DescriptorSerializer descriptorSerializer = new DescriptorSerializer();
        //jsonObject.add("lobby",descriptorSerializer.serialize(lobby.descriptor(),Descriptor.class,null));
        JsonArray alist = new JsonArray();
        lobby.entryList().forEach((a)->{
            alist.add(descriptorSerializer.serialize(a,Descriptor.class,null));
        });
        jsonObject.add("gameLobbyList",alist);
        return jsonObject;
    }
    private JsonObject toJson(Descriptor lobby,GameZone zone){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        JsonObject jzon = new JsonObject();
        jzon.addProperty("zoneId",zone.distributionKey());
        jzon.addProperty("name",zone.name()!=null?zone.name():lobby.name());
        jzon.addProperty("levelMatch",zone.levelMatch());
        jzon.addProperty("tag",lobby.tag());
        jzon.addProperty("rank",lobby.accessRank());
        jzon.addProperty("capacity",zone.capacity());
        jzon.addProperty("joinsOnStart",zone.joinsOnStart());
        jzon.addProperty("duration",zone.roundDuration()/60000);
        jzon.addProperty("levelLimit",zone.levelLimit()>0?zone.levelLimit():lobby.capacity());
        jzon.addProperty("playMode",zone.playMode());
        jzon.addProperty("disabled",lobby.disabled());
        jsonObject.add("zone",jzon);
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
        jsonObject.add("levels",jds);
        return jsonObject;
    }
}
