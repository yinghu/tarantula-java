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
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.platform.service.TokenValidatorProvider;
import com.tarantula.platform.util.OnAccessSerializer;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class AdminEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(AdminEventHandler.class);

    private String bucket;
    private EventService eventService;
    private TokenValidatorProvider tokenValidator;
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
            String token = exchange.header(Session.TARANTULA_TOKEN);
            log.warn("TOKEN->"+token);
            OnSession onSession = tokenValidator.tokenValidator().validateToken(token+1);
            String contentType = exchange.path().endsWith(".html")?"text/html":"text/javascript";
            byte[] ret = this.deploymentServiceProvider.resource(exchange.path().substring(1),null);
            exchange.onEvent(new ResponsiveEvent("","",ret,0,contentType,"",true));
            /**
            if(action.equals("onAdmin")){
                String accessKey = exchange.header(Session.TARANTULA_ACCESS_KEY);
                String name = exchange.header(Session.TARANTULA_NAME);
                String password = exchange.header(Session.TARANTULA_PASSWORD);
                byte[] eb= "{}".getBytes();
                if(tokenValidator.validateAccessKey(accessKey)){
                    AccessIndex accessIndex = accessIndexService.get(name);
                    if(accessIndex!=null){
                        _hex.put(exchange.id(),exchange);
                        OnAccess onAccess = new OnAccessTrack();
                        onAccess.property("password",password);
                        ServiceActionEvent event = new ServiceActionEvent(this.serverTopic,exchange.id(),this.builder.create().toJson(onAccess).getBytes());
                        event.action("onLogin");
                        event.systemId(accessIndex.distributionKey());
                        RoutingKey _routingKey = eventService.routingKey(accessIndex.distributionKey(),"index/user");
                        event.destination(_routingKey.route());
                        event.routingNumber(_routingKey.routingNumber());
                        this.eventService.publish(event);
                    }else{
                        exchange.onEvent(new ResponsiveEvent("","",eb,"admin",true));
                    }
                }
                else{
                    exchange.onEvent(new ResponsiveEvent("","",eb,"admin",true));
                }**/

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
        this.builder.registerTypeAdapter(OnAccessTrack.class,new OnAccessSerializer());
        this.serverTopic = UUID.randomUUID().toString();
        this.eventService.registerEventListener(this.serverTopic,this);
        log.info("Admin event handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    public void setup(ServiceContext tcx){
        this.eventService = tcx.eventService(Distributable.INTEGRATION_SCOPE);
        this.accessIndexService = tcx.accessIndexService();
        this.bucket = tcx.bucket();
        tokenValidator  = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
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
