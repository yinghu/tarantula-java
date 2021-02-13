package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.AccessControl;
import com.tarantula.platform.OnAccessTrack;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.service.*;
import com.tarantula.platform.util.OnAccessSerializer;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.UUID;


public class AdminEventHandler implements RequestHandler{

    private static TarantulaLogger log = JDKLogger.getLogger(AdminEventHandler.class);

    private EventService eventService;
    private TokenValidatorProvider tokenValidator;
    private DeploymentServiceProvider deploymentServiceProvider;
    private RecoverService recoverService;
    private String serverTopic;
    private GsonBuilder builder;
    private OnView invalidView;
    public AdminEventHandler(){

    }
    public String name(){
        return "/admin";
    }
    public void onRequest(OnExchange exchange){
        try{
            String token = exchange.header(Session.TARANTULA_TOKEN);
            OnSession onSession = tokenValidator.tokenValidator().validateToken(token);
            if(!recoverService.checkAccessControl(onSession.systemId(), AccessControl.account)){
                throw new RuntimeException("no access permission");
            }
            Content ret = this.deploymentServiceProvider.resource(exchange.path().substring(1),null);
            if(!ret.existed()){
                ret = this.deploymentServiceProvider.resource(invalidView.moduleResourceFile(),null);
            }
            exchange.onEvent(new ResponsiveEvent("","",ret.data(),0,ret.type(),"",true));
            deploymentServiceProvider.onUpdated(Metrics.REQUEST_COUNT,1);

        }catch (Exception ex){
            ex.printStackTrace();
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
        log.info("Admin event handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    public void setup(ServiceContext tcx){
        this.eventService = tcx.eventService(Distributable.INTEGRATION_SCOPE);
        this.recoverService = tcx.clusterProvider(Distributable.INTEGRATION_SCOPE).recoverService();
        tokenValidator  = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
        this.deploymentServiceProvider = (DeploymentServiceProvider)tcx.serviceProvider(DeploymentServiceProvider.NAME);
    }
    public  boolean onEvent(Event event){

        return true;
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
}
