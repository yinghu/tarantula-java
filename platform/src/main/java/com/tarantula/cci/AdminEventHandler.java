package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.OnAccessTrack;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.UDPConnection;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.event.ServerPushEvent;
import com.tarantula.platform.event.ServiceActionEvent;
import com.tarantula.platform.presence.User;
import com.tarantula.platform.service.AccessIndexService;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class AdminEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(AdminEventHandler.class);

    private String bucket;
    private EventService eventService;
    private TokenValidator tokenValidator;
    private DeploymentServiceProvider deploymentServiceProvider;
    private AccessIndexService accessIndexService;
    private String serverTopic;
    private final ConcurrentHashMap<String,OnExchange> _hex = new ConcurrentHashMap<>();
    private GsonBuilder builder;
    public AdminEventHandler(){

    }
    public String name(){
        return "/admin";
    }
    public void onRequest(OnExchange exchange){
        try{
            String action = exchange.header(Session.TARANTULA_ACTION);
            if(action.equals("onAdmin")){
                String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
                String name = exchange.header(Session.TARANTULA_NAME);
                String password = exchange.header(Session.TARANTULA_PASSWORD);
                byte[] eb= "{}".getBytes();
                if(tokenValidator.validateAccessKey(accessKey)){
                    //this.deploymentServiceProvider.onEndedUDPConnection(serverId,_payload);
                    AccessIndex accessIndex = accessIndexService.get(name);
                    if(accessIndex!=null){
                        _hex.put(exchange.id(),exchange);
                        OnAccess onAccess = new OnAccessTrack();

                        ServiceActionEvent event = new ServiceActionEvent(this.serverTopic,exchange.id(),new byte[0]);
                        event.action("onLogin");
                        event.systemId(accessIndex.distributionKey());
                        RoutingKey _routingKey = eventService.routingKey(accessIndex.distributionKey(),"index/user");
                        event.destination(_routingKey.route());
                        event.routingNumber(_routingKey.routingNumber());
                        this.eventService.publish(event);
                        //eb = this.builder.create().toJson(new ResponseHeader("onAdmin",accessIndex.distributionKey(),true)).getBytes();
                    }else{
                        exchange.onEvent(new ResponsiveEvent("","",eb,"admin",true));
                    }
                }
                else{
                    exchange.onEvent(new ResponsiveEvent("","",eb,"admin",true));
                }
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
        log.info("Admin event handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void setup(TokenValidator tokenValidator, EventService eventService, AccessIndexService accessIndexService, String bucket, DeploymentServiceProvider deploymentServiceProvider) {
        this.eventService = eventService;
        this.accessIndexService = accessIndexService;
        this.bucket = bucket;
        this.tokenValidator = tokenValidator;
        this.deploymentServiceProvider = deploymentServiceProvider;
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
