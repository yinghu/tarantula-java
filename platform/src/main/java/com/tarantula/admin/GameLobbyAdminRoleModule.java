package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.util.DescriptorSerializer;

import java.util.HashMap;
import java.util.List;

public class GameLobbyAdminRoleModule implements Module {
    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    private int maxGameLobbyCount;
    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if (session.action().equals("onGameLobbyList")){
            GameCluster gc = this.deploymentServiceProvider.gameCluster(session.name());
            if(gc!=null && !((boolean)gc.property(GameCluster.DISABLED))){
                Lobby lobby = this.deploymentServiceProvider.lobby((String) gc.property(GameCluster.GAME_LOBBY));
                session.write(toJson(lobby).toString().getBytes());
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"no lobby data ["+session.name()+"]").getBytes());
            }
        }
        else if(session.action().equals("onGameServiceList")){
            GameCluster gameCluster = deploymentServiceProvider.gameCluster(session.name());
            if(gameCluster.serviceLobby!=null) {
                session.write(toJson(gameCluster.serviceLobby.entryList()));
            }
            else{
                session.write(JsonUtil.toSimpleResponse(false,"game cluster not launched").getBytes());
            }
        }
        else if (session.action().equals("onAddLobby")){
            String[] query = session.name().split("#");
            String gameClusterId = query[0];
            int lobbyIndex = Integer.parseInt(query[1]);
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
            String[] query = session.name().split("#");
            boolean suc = this.deploymentServiceProvider.disableApplication(query[1]);
            session.write(JsonUtil.toSimpleResponse(suc,session.name()).getBytes());
        }
        else if(session.action().equals("onEnableLobby")){
            String[] query = session.name().split("#");
            boolean suc = this.deploymentServiceProvider.enableApplication(query[1]);
            session.write(JsonUtil.toSimpleResponse(suc,session.name()).getBytes());
        }
        else{
            session.write(JsonUtil.toSimpleResponse(false, session.action()+" operation not supported").getBytes());
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
    private byte[] toJson(List<Descriptor> existed){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        JsonArray array = new JsonArray();
        existed.forEach((a)->array.add(a.toJson()));
        jsonObject.add("serviceList",array);
        return jsonObject.toString().getBytes();
    }

}
