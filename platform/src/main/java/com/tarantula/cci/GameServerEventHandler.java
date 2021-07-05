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
import com.tarantula.platform.util.ConnectionDeserializer;
import com.tarantula.platform.util.ConnectionSerializer;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class GameServerEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(GameServerEventHandler.class);

    //private String bucket;
    private EventService eventService;
    private TokenValidatorProvider tokenValidatorProvider;
    private DeploymentServiceProvider deploymentServiceProvider;

    private String serverTopic;
    private final ConcurrentHashMap<String, OnExchange> _hex = new ConcurrentHashMap<>();
    private GsonBuilder builder;

    private DeployService deployService;
    public String name(){
        return "/server";
    }

    public void onRequest(OnExchange exchange) throws Exception{

        String action = exchange.header(Session.TARANTULA_ACTION);
        String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
        String serverId = exchange.header(Session.TARANTULA_SERVER_ID);
        String zoneId = exchange.header(Session.TARANTULA_ZONE_ID);
        String roomId = exchange.header(Session.TARANTULA_ROOM_ID);
        String type = exchange.header(Session.TARANTULA_NAME);//update action
        String connectionId = exchange.header(Session.TARANTULA_CONNECTION_ID);
        byte[] _payload = exchange.payload();
        String typeId = tokenValidatorProvider.validateGameClusterAccessKey(accessKey);
        if(typeId==null){
            throw new RuntimeException("Illegal access");
        }
        if(action.equals("onAck")){
            exchange.onEvent(new ResponsiveEvent("","","{}".getBytes(),true));
            deployService.ackServerPushEvent(serverId);
        }
        else if(action.equals("onStart")){//start game server
            JsonObject resp = new JsonObject();
            resp.addProperty("typeId",typeId);
            resp.addProperty("successful",true);
            Connection connection = builder.create().fromJson(new String(_payload),Connection.class);
            resp.addProperty("serverKey", Base64.getEncoder().encodeToString(this.deploymentServiceProvider.serverKey(connection)));
            resp.addProperty("connectionId",connection.connectionId());
            resp.addProperty("sequence",connection.sequence());
            ServerPushEvent pushEvent = new ServerPushEvent(this.serverTopic,serverId,serverId,this.builder.create().toJson(connection).getBytes());
            pushEvent.typeId(typeId);
            deployService.addServerPushEvent(pushEvent);
            JsonArray cids = new JsonArray();
            for(int i=1;i<=connection.maxConnections();i++){
                Connection conn = this.deploymentServiceProvider.distributionCallback().addConnection(typeId,toClientConnection(connection,connection.connectionId()+i));
                cids.add(conn.connectionId());
            }
            resp.add("connections",cids);
            exchange.onEvent(new ResponsiveEvent("","",resp.toString().getBytes(),true));
        }
        else if(action.equals("onConnection")){
            Connection connection = this.deploymentServiceProvider.distributionCallback().addConnection(serverId,Integer.parseInt(connectionId));
            exchange.onEvent(new ResponsiveEvent("", "", builder.create().toJson(connection).getBytes(), true));
        }
        else if(action.equals("onUpdate")){
            exchange.onEvent(new ResponsiveEvent("","", "{}".getBytes(),true));
            //publish event to zone subscription/trackId
            eventService.publish(new GameUpdateEvent(zoneId,roomId,type,_payload));
        }
        else if(action.equals("onStop")){//stop the game server
            deployService.removeServerPushEvent(serverId);
            _hex.forEach((k,v)->{//removed session if any
                if(v.id().equals(serverId)){
                    _hex.remove(k);
                }
            });
            exchange.onEvent(new ResponsiveEvent("","","{}".getBytes(),true));
        }
    }

    @Override
    public void start() throws Exception {
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(Connection.class,new ConnectionDeserializer());
        this.builder.registerTypeAdapter(Connection.class,new ConnectionSerializer());
        this.serverTopic = UUID.randomUUID().toString();
        this.eventService.registerEventListener(this.serverTopic,this);
        log.info("Game server event handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }

    public void setup(ServiceContext tcx){
        this.eventService = tcx.eventService(Distributable.INTEGRATION_SCOPE);
        this.deployService = tcx.clusterProvider(Distributable.INTEGRATION_SCOPE).deployService();
        this.tokenValidatorProvider = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
        this.deploymentServiceProvider = tcx.deploymentServiceProvider();
    }
    public  boolean onEvent(Event event){
        OnExchange hx = this._hex.get(event.sessionId());
        if(hx!=null){
           if(hx.onEvent(event)){ //remove on true marked as closed connect or session
               _hex.remove(event.sessionId());
           }
        }
        else{
           log.warn(event.toString()+" unexpected removed on game server push");
        }
        return true;
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
    private Connection toClientConnection(Connection connection,int connectionId){
        Connection conn = new ClientConnection();
        conn.type(connection.type());
        conn.serverId(connection.serverId());
        conn.host(connection.host());
        conn.port(connection.port());
        conn.secured(connection.secured());
        conn.connectionId(connectionId);
        return conn;
    }
}
