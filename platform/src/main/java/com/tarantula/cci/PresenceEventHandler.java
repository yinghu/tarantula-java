package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.icodesoftware.Distributable;
import com.icodesoftware.Event;
import com.icodesoftware.OnSession;
import com.icodesoftware.Session;
import com.icodesoftware.service.EventService;
import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.OnAccessTrack;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.service.*;
import com.tarantula.platform.util.OnAccessSerializer;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PresenceEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(PresenceEventHandler.class);

    private String bucket;
    private EventService eventService;
    private TokenValidatorProvider tokenValidator;
    private DeploymentServiceProvider deploymentServiceProvider;
    private AccessIndexService accessIndexService;
    private String serverTopic;
    private final ConcurrentHashMap<String,OnExchange> _hex = new ConcurrentHashMap<>();
    private GsonBuilder builder;
    private OnView invalidView;
    public PresenceEventHandler(){

    }
    public String name(){
        return "/presence";
    }
    public void onRequest(OnExchange exchange){
        try{
            String token = exchange.header(Session.TARANTULA_TOKEN);
            OnSession onSession = tokenValidator.tokenValidator().validateToken(token);
            String contentType = exchange.path().endsWith(".html")?"text/html":"text/javascript";
            byte[] ret = this.deploymentServiceProvider.resource(exchange.path().substring(1),null);
            if(ret.length==0){
                ret = this.deploymentServiceProvider.resource(invalidView.moduleResourceFile(),null);
            }
            exchange.onEvent(new ResponsiveEvent("","",ret,0,contentType,"",true));
            deploymentServiceProvider.onUpdated(Metrics.REQUEST_COUNT,1);
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
        this.invalidView = this.deploymentServiceProvider.onView(OnView.INVALID_VIEW_ID);
        log.info("Presence event handler started");
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
