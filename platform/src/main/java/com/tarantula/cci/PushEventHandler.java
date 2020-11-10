package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.event.ServerPushEvent;
import com.tarantula.platform.util.ConnectionDeserializer;
import com.tarantula.platform.util.ConnectionSerializer;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class PushEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(PushEventHandler.class);

    //private String bucket;
    private EventService eventService;
    private TokenValidatorProvider tokenValidatorProvider;

    private String serverTopic;
    private final ConcurrentHashMap<String,OnExchange> _hex = new ConcurrentHashMap<>();
    private GsonBuilder builder;
    private Connection endpoint;
    private DeployService deployService;
    private DeploymentServiceProvider deploymentServiceProvider;
    public String name(){
        return "/push";
    }
    public void onRequest(OnExchange exchange){
        try{
            String action = exchange.header(Session.TARANTULA_ACTION);
            String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
            String serverId = exchange.header(Session.TARANTULA_SERVER_ID);
            byte[] _payload = exchange.payload();
            String typeId = this.tokenValidatorProvider.validateAccessKey(accessKey);
            if(typeId==null){
                throw new RuntimeException("Illegal access");
            }
            if(action.equals("onAck")){
                exchange.onEvent(new ResponsiveEvent("","","{}".getBytes(),"ack",true));
                deployService.ackServerPushEvent(serverId);
            }
            else if(action.equals("onStart")){
                JsonObject resp = new JsonObject();
                resp.addProperty("typeId",typeId);
                resp.addProperty("successful",true);
                Connection connection = builder.create().fromJson(new String(_payload),Connection.class);
                resp.addProperty("serverKey",Base64.getEncoder().encodeToString(this.deploymentServiceProvider.serverKey(connection)));
                resp.addProperty("connectionId",connection.server().connectionId());
                resp.addProperty("sequence",connection.server().sequence());
                ServerPushEvent pushEvent = new ServerPushEvent(this.serverTopic,serverId,serverId,this.builder.create().toJson(connection).getBytes());
                pushEvent.typeId(typeId);
                deployService.addServerPushEvent(pushEvent);
                for(int i=0;i<connection.maxConnections();i++){
                    this.deploymentServiceProvider.distributionCallback().onConnection(typeId,connection);
                }
                exchange.onEvent(new ResponsiveEvent("","",resp.toString().getBytes(),"start",true));
            }
            else if(action.equals("onConnection")){

                exchange.onEvent(new ResponsiveEvent("","","{}".getBytes(),"onConnection",true));
            }
            else if(action.equals("onStop")){
                deployService.removeServerPushEvent(serverId);
                _hex.forEach((k,v)->{//removed session if any
                    if(v.id().equals(serverId)){
                        _hex.remove(k);
                    }
                });
                exchange.onEvent(new ResponsiveEvent("","",_payload,"start",true));
            }

            //start of socket connection methods
            else if(action.equals("onTicket")){
                byte[] et;
                if(this.tokenValidatorProvider.validateAccessKey(accessKey)!=null){
                    JsonObject jo = new JsonObject();
                    jo.addProperty("ticket", tokenValidatorProvider.ticket(serverId,1,5));
                    jo.addProperty("host",endpoint.host());
                    jo.addProperty("port",endpoint.port());
                    jo.addProperty("successful",true);
                    et = jo.toString().getBytes();
                }
                else{
                    ResponseHeader err = new ResponseHeader("onTicket","invalid access key",false);
                    et = builder.create().toJson(err).getBytes();
                }
                exchange.onEvent(new ResponsiveEvent("","",et,"push",true));
            }
            else if(action.equals("onConnect")){//access key
                if(tokenValidatorProvider.validateTicket(serverId,1,accessKey)){
                    String sid = exchange.id();
                    _hex.put(sid,exchange);
                    ServerPushEvent pushEvent = new ServerPushEvent(this.serverTopic,sid,serverId,_payload);
                    deployService.addServerPushEvent(pushEvent);
                }
                else{
                    log.warn("Invalid ticket");
                    exchange.close();
                }
            }
            else if(action.equals("onDisconnect")){//no more access key check event from server socket
                _hex.forEach((k,v)->{
                    if(v.header(Session.TARANTULA_SERVER_ID).equals(serverId)){
                        _hex.remove(k);
                        deployService.removeServerPushEvent(serverId);
                    }
                });
            }
            //end of socket connection methods
        }catch (Exception ex){
            ex.printStackTrace();
            _hex.remove(exchange.id()); //removed cache on any errors
            exchange.onError(ex,ex.getMessage());
        }
    }

    @Override
    public void start() throws Exception {
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(Connection.class,new ConnectionSerializer());
        this.builder.registerTypeAdapter(Connection.class,new ConnectionDeserializer());
        this.serverTopic = UUID.randomUUID().toString();
        this.eventService.registerEventListener(this.serverTopic,this);
        log.info("TCP Push event handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }

    public void setup(ServiceContext tcx){
        this.eventService = tcx.eventService(Distributable.INTEGRATION_SCOPE);
        this.deployService = tcx.clusterProvider(Distributable.INTEGRATION_SCOPE).deployService();
        this.endpoint = tcx.endpoint();
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
           log.warn(event.toString()+" unexpected removed on server push");
        }
        return true;
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
}
