package com.tarantula.cci;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;

import com.tarantula.platform.event.ResponsiveEvent;


import java.util.concurrent.ConcurrentHashMap;


public class DevelopmentEventHandler extends AbstractRequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(DevelopmentEventHandler.class);
    private final static String METRICS_CATEGORY = "httpDevelopmentCount";
    private DeploymentServiceProvider deploymentServiceProvider;

    private ConcurrentHashMap<Long,PendingOperation> uMap = new ConcurrentHashMap<>();

    public DevelopmentEventHandler(){
        super(true);
    }
    public String name(){
        return DEVELOPMENT_PATH;
    }
    public void onRequest(OnExchange exchange) throws Exception{
        super.onRequest(exchange);
        String token = exchange.header(Session.TARANTULA_TOKEN);
        String action = exchange.header(Session.TARANTULA_ACTION);
        OnSession onSession = tokenValidator.tokenValidator().validateToken(token);
        if(action.equals("onDataBootstrap")) {
            uMap.put(exchange.id(),new DataBootstrapOperation(this.deploymentServiceProvider));
            checkPermission(onSession,exchange.id(),"role/sudo");
        }
        else if(action.equals("onDevelopment")){
            checkPermission(onSession,exchange.id(),"role/admin");
        }
    }
    @Override
    public void start() throws Exception {
        super.start();
        log.info("Development handler started");
    }


    public void setup(ServiceContext tcx){
        super.setup(tcx);
        this.deploymentServiceProvider = tcx.deploymentServiceProvider();
    }
    public boolean onEvent(Event event){
        JsonObject resp = JsonUtil.parse(event.payload());
        if(!resp.get("successful").getAsBoolean()){
            uMap.remove(event.sessionId());
            return super.onEvent(event);
        }
        OnExchange exchange = eMap.get(event.sessionId());
        try {
            PendingOperation pendingOperation = uMap.remove(event.sessionId());
            pendingOperation.execute(exchange);
            eMap.remove(event.sessionId());
            return false;
        }catch (Exception ex){
            log.error("failed on development handler",ex);
            return super.onEvent(new ResponsiveEvent("", event.sessionId(), JsonUtil.toSimpleResponse(false,ex.getMessage()).getBytes(),0,"text/html",true));
        }
    }

    public String metricsCategory(){
        return METRICS_CATEGORY;
    }


}
