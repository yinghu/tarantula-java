package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.Item;
import com.tarantula.platform.item.ItemQuery;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.util.SystemUtil;

import java.util.List;

public class GameStoreAdminModule implements Module {
    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate onUpdate) throws Exception {
        if(session.action().equals("onList")){
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(session.name());
            Descriptor app = this.loadDescriptor(gameCluster,this.context.descriptor().category());
            ApplicationPreSetup preSetup = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
            List<Item> items = preSetup.list(this.context,app,new ItemQuery());
            session.write(toJson(items).toString().getBytes());
        }
        else if (session.action().equals("onRegister")){
            String[] ks = session.name().split("#");
            GameCluster gameCluster = this.deploymentServiceProvider.gameCluster(ks[0]);
            Item app = new Item();
            app.distributionKey(ks[1]);
            Descriptor desc = loadDescriptor(gameCluster,this.context.descriptor().category());
            if(SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME)).load(context,desc,app)){
                session.write(JsonUtil.toSimpleResponse(true,ks[1]).getBytes());
                GameServiceProvider gameServiceProvider = this.context.serviceProvider((String) gameCluster.property(GameCluster.GAME_SERVICE));
                gameServiceProvider.configurationServiceProvider().register(app);
                session.write(JsonUtil.toSimpleResponse(true,"item live").getBytes());
            }
           else{
               session.write(JsonUtil.toSimpleResponse(false,"failed to save item").getBytes());
           }
        }
        else if(session.action().equals("onLoad")){

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
        this.context.log("game store admin module started", OnLog.WARN);
    }
    private Descriptor loadDescriptor(GameCluster gameCluster, String category){
        String lobbyTypeId = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        Lobby lobby = this.deploymentServiceProvider.lobby(lobbyTypeId);
        Descriptor[] descriptors = {null};
        lobby.entryList().forEach((d)->{
            if(d.category().equals(category)){
                descriptors[0]=d;
            }
        });
        return descriptors[0];
    }
    private JsonObject toJson(List<Item> itemList){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        JsonArray alist = new JsonArray();
        itemList.forEach((v)->{
            alist.add(v.toJson());
        });
        jsonObject.add("itemList",alist);
        return jsonObject;
    }
}
