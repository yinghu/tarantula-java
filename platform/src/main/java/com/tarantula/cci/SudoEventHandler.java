package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.OnAccessTrack;
import com.tarantula.platform.ResponseHeader;

import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.util.OnAccessSerializer;
import com.tarantula.platform.util.ResponseSerializer;



public class SudoEventHandler extends AbstractRequestHandler{

    private static TarantulaLogger log = JDKLogger.getLogger(SudoEventHandler.class);
    private DeploymentServiceProvider deploymentServiceProvider;
    private GsonBuilder builder;
    private OnView invalidView;

    public SudoEventHandler(){
        super(true);
    }
    public String name(){
        return SUDO_PATH;
    }
    public void onRequest(OnExchange exchange) throws Exception{
        super.onRequest(exchange);
        String token = exchange.header(Session.TARANTULA_TOKEN);
        OnSession id = tokenValidator.tokenValidator().validateToken(token);
        checkPermission(id,token,exchange.id(),"role/sudo");
    }

    @Override
    public void start() throws Exception {
        super.start();
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(OnAccessTrack.class,new OnAccessSerializer());
        this.invalidView = this.deploymentServiceProvider.view(OnView.INVALID_VIEW_ID);
        log.info("Sudo service event handler started");
    }

    public void setup(ServiceContext tcx){
        super.setup(tcx);
        this.deploymentServiceProvider = (DeploymentServiceProvider)tcx.serviceProvider(DeploymentServiceProvider.NAME);
    }
    public  boolean onEvent(Event event){
        JsonObject resp = JsonUtil.parse(event.payload());
        if(!resp.get("successful").getAsBoolean()){
            return super.onEvent(event);
        }
        OnExchange exchange = eMap.get(event.sessionId());
        Content ret = this.deploymentServiceProvider.resource(exchange.path().substring(1));
        if(!ret.existed()){
            ret = this.deploymentServiceProvider.resource(invalidView.moduleResourceFile());
        }
        return super.onEvent(new ResponsiveEvent("",event.sessionId(),ret.data(),0,ret.type(),true));
    }
}
