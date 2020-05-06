package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.UDPConnection;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.event.ServerPushEvent;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.platform.service.TokenValidatorProvider;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class PushEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(PushEventHandler.class);

    private String bucket;
    private EventService eventService;
    private TokenValidator tokenValidator;
    private DeploymentServiceProvider deploymentServiceProvider;

    private String serverTopic;
    private final ConcurrentHashMap<String,OnExchange> _hex = new ConcurrentHashMap<>();
    private GsonBuilder builder;
    public PushEventHandler(){

    }
    public String name(){
        return "/push";
    }
    public void onRequest(OnExchange exchange){
        try{
            String action = exchange.header(Session.TARANTULA_ACTION);
            byte[] _payload = exchange.payload();
            if(action.equals("onConnect")){
                //log.warn("push->"+exchange.path()+"/"+exchange.header("serverId")+"/"+exchange.id()+"/"+"/"+action+"/"+exchange.streaming());
                String sid = exchange.id();
                _hex.put(sid,exchange);
                ServerPushEvent pushEvent = new ServerPushEvent(this.serverTopic,sid,false);
                pushEvent.bucket(this.bucket);
                pushEvent.clientId(exchange.header("serverId"));
                pushEvent.owner(this.eventService.subscription());
                pushEvent.destination(DeploymentServiceProvider.DEPLOY_TOPIC);
                pushEvent.payload(_payload);
                eventService.publish(pushEvent);
            }
            else if(action.equals("onDisconnect")){
                String serverId = exchange.header("serverId");
                _hex.forEach((k,v)->{
                    if(v.header("serverId").equals(serverId)){
                        _hex.remove(k);
                        ServerPushEvent pushEvent = new ServerPushEvent(this.serverTopic,k,true);
                        pushEvent.bucket(this.bucket);
                        pushEvent.clientId(exchange.header("serverId"));
                        pushEvent.owner(this.eventService.subscription());
                        pushEvent.destination(DeploymentServiceProvider.DEPLOY_TOPIC);
                        pushEvent.payload(_payload);
                        eventService.publish(pushEvent);
                    }
                });
            }
            else if(action.equals("onRegistered")){ //dedicated server register on cluster
                String typeId = exchange.header("Tarantula-type-id");
                String host = exchange.header("Tarantula-host");
                int port = Integer.parseInt(exchange.header("Tarantula-port"));
                String serverId = exchange.header("Tarantula-server-id");
                String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
                if(tokenValidator.validateAccessKey(accessKey)){
                    Connection connection = new UDPConnection(serverId,host,port);
                    this.deploymentServiceProvider.onUDPConnection(typeId,connection);
                }
                byte[] eb = this.builder.create().toJson(new ResponseHeader("onRegistered","ok",true)).getBytes();
                exchange.onEvent(new ResponsiveEvent("","",eb,"dedicated",true));
            }
            else if(action.equals("onStarted")){
                String serverId = exchange.header("Tarantula-server-id");
                String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
                byte[] eb = "{}".getBytes();
                if(tokenValidator.validateAccessKey(accessKey)) {
                    eb = this.deploymentServiceProvider.onStartedUDPConnection(serverId);
                }
                exchange.onEvent(new ResponsiveEvent("","",eb,"dedicated",true));
            }
            else if(action.equals("onUpdated")){
                String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
                String serverId = exchange.header("Tarantula-server-id");
                if(tokenValidator.validateAccessKey(accessKey)){
                    this.deploymentServiceProvider.onUpdatedUDPConnection(serverId,_payload);
                }
                byte[] eb = this.builder.create().toJson(new ResponseHeader("onUpdated","ok",true)).getBytes();
                exchange.onEvent(new ResponsiveEvent("","",eb,"dedicated",true));
            }
            else if(action.equals("onEnded")){
                String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
                String serverId = exchange.header("Tarantula-server-id");
                if(tokenValidator.validateAccessKey(accessKey)){
                    this.deploymentServiceProvider.onEndedUDPConnection(serverId,_payload);
                }
                byte[] eb = this.builder.create().toJson(new ResponseHeader("onEnded","ok",true)).getBytes();
                exchange.onEvent(new ResponsiveEvent("","",eb,"dedicated",true));
            }
        }catch (Exception ex){
            ex.printStackTrace();
            _hex.remove(exchange.id()); //removed cache on any errors
            exchange.onError(ex,"Bad request");
        }
    }

    @Override
    public void start() throws Exception {
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.serverTopic = UUID.randomUUID().toString();
        this.eventService.registerEventListener(this.serverTopic,this);
        log.info("Push event handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }

    public void setup(ServiceContext tcx){
        this.eventService = tcx.eventService(Distributable.INTEGRATION_SCOPE);
        //this.accessIndexService = tcx.accessIndexService();
        this.bucket = tcx.bucket();
        TokenValidatorProvider tp = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
        this.tokenValidator = tp.tokenValidator();
        this.deploymentServiceProvider = (DeploymentServiceProvider)tcx.serviceProvider(DeploymentServiceProvider.NAME);
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
