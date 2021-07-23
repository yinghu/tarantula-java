package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.GameLobby;
import com.tarantula.game.GameZone;
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
        else if(session.action().equals("onGameLobby")){
            Map<String,Object> cmd = JsonUtil.toMap(payload);
            String gameClusterId = (String) cmd.get("gameClusterId");
            String applicationId = (String) cmd.get("applicationId");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(gameClusterId);
            Lobby lobby = this.deploymentServiceProvider.lobby((String)gameCluster.property(GameCluster.GAME_LOBBY));
            Descriptor[] descriptors = {null};
            lobby.entryList().forEach((a)->{
                if(a.distributionKey().equals(applicationId)){
                    descriptors[0] = a;
                }
            });
            GameZone zone = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).load(this.context,descriptors[0]);
            GameLobby gameLobby = new GameLobby();
            gameLobby.lobby = descriptors[0];
            gameLobby.zone = zone;
            session.write(gameLobby.toJson().toString().getBytes());
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
                    desc.index((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
                    String configName = (String) gameCluster.property(GameCluster.MODE);
                    if(this.deploymentServiceProvider.createApplication(desc,(String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME),configName,false)){
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
}
