package com.tarantula.cci;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.JsonUtil;

import com.tarantula.platform.event.ResponsiveEvent;


public class AdminEventHandler extends AbstractRequestHandler{

    private static TarantulaLogger log = JDKLogger.getLogger(AdminEventHandler.class);
    private final static String METRICS_CATEGORY = "httpAdminCount";
    private DeploymentServiceProvider deploymentServiceProvider;

    private OnView invalidView;

    public AdminEventHandler(){
        super(true);
    }

    public String name(){
        return ADMIN_PATH;
    }

    public String metricsCategory(){
        return METRICS_CATEGORY;
    }
    public void onRequest(OnExchange exchange) throws Exception{
        super.onRequest(exchange);
        String token = exchange.header(Session.TARANTULA_TOKEN);
        OnSession onSession = tokenValidator.tokenValidator().validateToken(token);
        checkPermission(onSession,exchange.id(),"role/admin");
    }

    @Override
    public void start() throws Exception {
        super.start();
        this.invalidView = this.deploymentServiceProvider.view(OnView.INVALID_VIEW_ID);
        log.info("Admin event handler started");
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
