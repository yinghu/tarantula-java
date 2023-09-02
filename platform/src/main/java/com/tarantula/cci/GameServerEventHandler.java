package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.room.ChannelStub;
import com.tarantula.platform.room.ConnectionStub;
import com.tarantula.platform.util.ChannelDeserializer;
import com.tarantula.platform.util.ConnectionDeserializer;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.Base64;


public class GameServerEventHandler extends AbstractRequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(GameServerEventHandler.class);

    private DeploymentServiceProvider deploymentServiceProvider;

    private GsonBuilder builder;

    public GameServerEventHandler(){
        super(false);
    }
    public String name(){
        return GAME_SERVER_PATH;
    }

    public void onRequest(OnExchange exchange) throws Exception{

        String action = exchange.header(Session.TARANTULA_ACTION);
        String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
        String serverId = exchange.header(Session.TARANTULA_SERVER_ID);
        String name = exchange.header(Session.TARANTULA_NAME);
        byte[] _payload = exchange.payload();
        GameCluster gameCluster = tokenValidator.validateGameClusterAccessKey(accessKey);
        if(gameCluster==null) throw new RuntimeException("Illegal access");
        String typeId = gameCluster.gameLobbyName;
        if(action.equals("onConnect")){//start game server
            ConnectionStub connection = builder.create().fromJson(new String(_payload),ConnectionStub.class);
            byte[] serverKey = this.deploymentServiceProvider.serverKey(typeId);
            connection.configurationTypeId(typeId);
            connection.serverKey = serverKey;
            OnAccess onAccess = this.deploymentServiceProvider.registerConnection(connection);
            boolean suc = onAccess!=null;
            JsonObject resp = new JsonObject();
            resp.addProperty("successful",suc);
            if(suc) {
                resp.addProperty("typeId",typeId);
                resp.addProperty("serverKey", Base64.getEncoder().encodeToString(serverKey));
                resp.addProperty("sessionTimeout",(int)onAccess.property("sessionTimeout"));
                resp.addProperty("capacity",(int)onAccess.property("capacity"));
                resp.addProperty("joinsOnStart",(int)onAccess.property("joinsOnStart"));
                resp.addProperty("duration",(long)onAccess.property("duration"));
                resp.addProperty("overtime",(long)onAccess.property("overtime"));
                resp.addProperty("gameModule",(String)onAccess.property("gameModule"));
            }
            exchange.onEvent(new ResponsiveEvent("","",resp.toString().getBytes(),true));
        }
        else if(action.equals("onChannel")){
            ChannelStub channel = this.builder.create().fromJson(new String(_payload),ChannelStub.class);
            channel.serverId = serverId;
            channel.configurationTypeId(typeId);
            boolean suc = deploymentServiceProvider.registerChannel(channel);
            JsonObject resp = new JsonObject();
            resp.addProperty("successful",suc);
            exchange.onEvent(new ResponsiveEvent("", "",resp.toString().getBytes(), true));
        }
        else if(action.equals("onPing")){
            JsonObject resp = new JsonObject();
            resp.addProperty("successful",true);
            exchange.onEvent(new ResponsiveEvent("", "",resp.toString().getBytes(), true));
            deploymentServiceProvider.verifyConnection(typeId,serverId);
        }
        else if(action.equals("onStart")){//stop the game server
            ConnectionStub connection = builder.create().fromJson(new String(_payload),ConnectionStub.class);
            connection.configurationTypeId(typeId);
            this.deploymentServiceProvider.startConnection(connection);
            JsonObject resp = new JsonObject();
            resp.addProperty("typeId",typeId);
            resp.addProperty("successful",true);
            exchange.onEvent(new ResponsiveEvent("","",resp.toString().getBytes(),true));
        }
        else if(action.equals("onStop")){//stop the game server
            ConnectionStub connection = builder.create().fromJson(new String(_payload),ConnectionStub.class);
            connection.configurationTypeId(typeId);
            this.deploymentServiceProvider.stopConnection(connection);
            JsonObject resp = new JsonObject();
            resp.addProperty("typeId",typeId);
            resp.addProperty("successful",true);
            exchange.onEvent(new ResponsiveEvent("","",resp.toString().getBytes(),true));
        }
        else if(action.equals("onUpdate")){
            JsonObject resp = new JsonObject();
            resp.addProperty("typeId",typeId);
            resp.addProperty("successful",true);
            this.deploymentServiceProvider.updateRoom(typeId,name,_payload);
            exchange.onEvent(new ResponsiveEvent("","",resp.toString().getBytes(),true));
        }
    }

    @Override
    public void start() throws Exception {
        super.start();
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(ConnectionStub.class,new ConnectionDeserializer());
        this.builder.registerTypeAdapter(ChannelStub.class,new ChannelDeserializer());
        log.info("Game server event handler started");
    }


    public void setup(ServiceContext tcx){
        super.setup(tcx);
        this.deploymentServiceProvider = tcx.deploymentServiceProvider();
    }
}
