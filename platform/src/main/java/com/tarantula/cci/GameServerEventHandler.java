package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.ClientConnection;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.event.GameUpdateEvent;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.event.ServerPushEvent;
import com.tarantula.platform.util.ChannelDeserializer;
import com.tarantula.platform.util.ConnectionDeserializer;
import com.tarantula.platform.util.ConnectionSerializer;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class GameServerEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(GameServerEventHandler.class);

    //private String bucket;
    private TokenValidatorProvider tokenValidatorProvider;
    private DeploymentServiceProvider deploymentServiceProvider;

    private GsonBuilder builder;

    public String name(){
        return GAME_SERVER_PATH;
    }

    public void onRequest(OnExchange exchange) throws Exception{

        String action = exchange.header(Session.TARANTULA_ACTION);
        String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
        String serverId = exchange.header(Session.TARANTULA_SERVER_ID);
        byte[] _payload = exchange.payload();
        String typeId = tokenValidatorProvider.validateGameClusterAccessKey(accessKey);
        if(typeId==null){
            throw new RuntimeException("Illegal access");
        }
        if(action.equals("onStart")){//start game server
            JsonObject resp = new JsonObject();
            resp.addProperty("typeId",typeId);
            resp.addProperty("successful",true);
            Connection connection = builder.create().fromJson(new String(_payload),Connection.class);
            resp.addProperty("serverKey", Base64.getEncoder().encodeToString(this.deploymentServiceProvider.serverKey(connection)));
            connection.configurationTypeId(typeId);
            this.deploymentServiceProvider.register(connection);
            exchange.onEvent(new ResponsiveEvent("","",resp.toString().getBytes(),true));
        }
        else if(action.equals("onChannel")){
            Channel channel = this.builder.create().fromJson(new String(_payload),Channel.class);
            this.deploymentServiceProvider.distributionCallback().addChannel(serverId,channel);
            JsonObject resp = new JsonObject();
            resp.addProperty("typeId",typeId);
            resp.addProperty("successful",true);
            exchange.onEvent(new ResponsiveEvent("", "",_payload, true));
        }
        else if(action.equals("onStop")){//stop the game server
            Connection connection = builder.create().fromJson(new String(_payload),Connection.class);
            connection.configurationTypeId(typeId);
            this.deploymentServiceProvider.release(connection);
            JsonObject resp = new JsonObject();
            resp.addProperty("typeId",typeId);
            resp.addProperty("successful",true);
            exchange.onEvent(new ResponsiveEvent("","",resp.toString().getBytes(),true));
        }
    }

    @Override
    public void start() throws Exception {
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(Connection.class,new ConnectionDeserializer());
        this.builder.registerTypeAdapter(Connection.class,new ConnectionSerializer());
        this.builder.registerTypeAdapter(Channel.class,new ChannelDeserializer());
        log.info("Game server event handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }

    public void setup(ServiceContext tcx){
        this.tokenValidatorProvider = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
        this.deploymentServiceProvider = tcx.deploymentServiceProvider();
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }
}
