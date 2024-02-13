package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.protocol.GameServerListener;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.Base64Util;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.room.ChannelStub;
import com.tarantula.platform.room.ConnectionStub;
import com.tarantula.platform.util.ChannelDeserializer;
import com.tarantula.platform.util.ConnectionDeserializer;
import com.tarantula.platform.util.ResponseSerializer;

public class GameServerEventHandler extends AbstractRequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(GameServerEventHandler.class);
    private final static String METRICS_CATEGORY = "httpGameServerCount";
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
        if(action.equals("onTicket")){
            JsonObject resp = JsonUtil.parse(_payload);
            boolean valid = tokenValidator.validateTicket(resp.get("systemId").getAsLong(),resp.get("stub").getAsLong(),resp.get("ticket").getAsString());
            resp = new JsonObject();
            resp.addProperty("successful",valid);
            exchange.onEvent(new ResponsiveEvent("",0,resp.toString().getBytes(),true));
        }
        else if(action.equals("onConnect")){//start game server
            GameServerListener gameServerListener = deploymentServiceProvider.gameServerListener(typeId);
            JsonObject resp = new JsonObject();
            if(gameServerListener!=null) {
                ConnectionStub connection = builder.create().fromJson(new String(_payload), ConnectionStub.class);
                byte[] serverKey = this.deploymentServiceProvider.serverKey(typeId);
                connection.configurationTypeId(typeId);
                connection.serverKey = serverKey;
                OnAccess onAccess = gameServerListener.onConnection(connection);
                resp.addProperty("successful", onAccess.successful());
                if (onAccess.successful()) {
                    resp.addProperty("typeId", typeId);
                    resp.addProperty("serverKey", Base64Util.toBase64String(serverKey));
                    resp.addProperty("sessionTimeout", (int) onAccess.property("sessionTimeout"));
                    resp.addProperty("capacity", (int) onAccess.property("capacity"));
                    resp.addProperty("joinsOnStart", (int) onAccess.property("joinsOnStart"));
                    resp.addProperty("duration", (long) onAccess.property("duration"));
                    resp.addProperty("overtime", (long) onAccess.property("overtime"));
                    resp.addProperty("gameServiceProvider",(String)onAccess.property("gameServiceProvider"));
                } else {
                    resp.addProperty("message", onAccess.message());
                }
            }
            else{
                resp.addProperty("successful",false);
                resp.addProperty("message","game server listener not available");
            }
            exchange.onEvent(new ResponsiveEvent("",0,resp.toString().getBytes(),true));
        }
        else if(action.equals("onChannel")){
            GameServerListener gameServerListener = deploymentServiceProvider.gameServerListener(typeId);
            JsonObject resp = new JsonObject();
            if(gameServerListener!=null){
                ChannelStub channel = this.builder.create().fromJson(new String(_payload),ChannelStub.class);
                channel.serverId = serverId;
                channel.configurationTypeId(typeId);
                boolean suc = gameServerListener.onChannel(channel);
                resp.addProperty("successful",suc);
            }
            else{
                resp.addProperty("successful",false);
            }
            exchange.onEvent(new ResponsiveEvent("", 0,resp.toString().getBytes(), true));
        }
        else if(action.equals("onPing")){
            JsonObject resp = new JsonObject();
            resp.addProperty("successful",true);
            exchange.onEvent(new ResponsiveEvent("", 0,resp.toString().getBytes(), true));
            serviceContext.clusterProvider().deployService().onVerifyConnection(typeId,serverId);
        }
        else if(action.equals("onStart")){//stop the game server
            GameServerListener gameServerListener = deploymentServiceProvider.gameServerListener(typeId);
            JsonObject resp = new JsonObject();
            if(gameServerListener!=null){
                ConnectionStub connection = builder.create().fromJson(new String(_payload),ConnectionStub.class);
                connection.configurationTypeId(typeId);
                gameServerListener.onStartConnection(connection);
                resp.addProperty("typeId",typeId);
                resp.addProperty("successful",true);
            }
            else{
                resp.addProperty("successful",false);
            }
            exchange.onEvent(new ResponsiveEvent("",0,resp.toString().getBytes(),true));
        }
        else if(action.equals("onStop")){//stop the game server
            GameServerListener gameServerListener = deploymentServiceProvider.gameServerListener(typeId);
            JsonObject resp = new JsonObject();
            if(gameServerListener!=null){
                ConnectionStub connection = builder.create().fromJson(new String(_payload),ConnectionStub.class);
                connection.configurationTypeId(typeId);
                gameServerListener.onDisConnection(connection);
                resp.addProperty("typeId",typeId);
                resp.addProperty("successful",true);
            }else{
                resp.addProperty("successful",false);
            }
            exchange.onEvent(new ResponsiveEvent("",0,resp.toString().getBytes(),true));
        }
        else if(action.equals("onUpdate")){
            GameServerListener gameServerListener = deploymentServiceProvider.gameServerListener(typeId);
            JsonObject resp = new JsonObject();
            if(gameServerListener!=null){
                resp.addProperty("typeId",typeId);
                resp.addProperty("successful",true);
                gameServerListener.onUpdate(name,_payload);
            }
            else{
                resp.addProperty("successful",false);
            }
            exchange.onEvent(new ResponsiveEvent("",0,resp.toString().getBytes(),true));
        }
        else if(action.equals("onAction")) {
            String suggestedTypeId = exchange.header(Session.TARANTULA_TYPE_ID);
            JsonObject resp = new JsonObject();
            resp.addProperty("typeId", suggestedTypeId);
            resp.addProperty("successful", true);

            var event = new ResponsiveEvent("",0,resp.toString().getBytes(),true);
            exchange.onEvent(event);
            if(suggestedTypeId.equals(gameCluster.typeId())){
                event.typeId(suggestedTypeId);
                event.property(OnAccess.TYPE_ID, "onAction");
                event.property(OnAccess.PAYLOAD, _payload);
                this.deploymentServiceProvider.onGameClusterEvent(event);
            }
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
    public String metricsCategory(){
        return METRICS_CATEGORY;
    }
}
