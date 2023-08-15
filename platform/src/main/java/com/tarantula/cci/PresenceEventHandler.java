package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.AccessControl;
import com.tarantula.platform.OnAccessTrack;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.util.OnAccessSerializer;
import com.tarantula.platform.util.ResponseSerializer;


public class PresenceEventHandler extends AbstractRequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(PresenceEventHandler.class);


    private TokenValidatorProvider tokenValidator;
    private DeploymentServiceProvider deploymentServiceProvider;
    private GsonBuilder builder;
    private OnView invalidView;
    public PresenceEventHandler(){
        super(false);
    }
    public String name(){
        return PRESENCE_PATH;
    }
    public void onRequest(OnExchange exchange) throws Exception{
        String token = exchange.header(Session.TARANTULA_TOKEN);
        OnSession onSession = tokenValidator.tokenValidator().validateToken(token);
        if(tokenValidator.role(onSession.systemId()).accessControl()<AccessControl.player.accessControl()){
            throw new RuntimeException("no access permission");
        }
        Content ret = this.deploymentServiceProvider.resource(exchange.path().substring(1));
        if(!ret.existed()){
            ret = this.deploymentServiceProvider.resource(invalidView.moduleResourceFile());
        }
        exchange.onEvent(new ResponsiveEvent("","",ret.data(),0,ret.type(),true));
    }

    @Override
    public void start() throws Exception {
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(OnAccessTrack.class,new OnAccessSerializer());
        this.invalidView = this.deploymentServiceProvider.view(OnView.INVALID_VIEW_ID);
        log.info("Presence event handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    public void setup(ServiceContext tcx){
        this.tokenValidator  = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
        this.deploymentServiceProvider = (DeploymentServiceProvider)tcx.serviceProvider(DeploymentServiceProvider.NAME);
    }
    public  boolean onEvent(Event event){
             return true;
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
    public boolean deployable(){return true;}
}
