package com.tarantula.cci;

import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.service.AccessIndexService;
import com.tarantula.platform.service.DeploymentServiceProvider;


public class ResourceEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(ResourceEventHandler.class);


    private DeploymentServiceProvider deploymentServiceProvider;

    public ResourceEventHandler(){
    }
    public String name(){
        return "/resource";
    }
    public void onRequest(OnExchange exchange){
        String path = exchange.path();
        byte[] _load = this.deploymentServiceProvider.resource(path.substring(1).replace("resource","web"));
        exchange.onEvent(new ResponsiveEvent("","",_load,0,"text/javascript","",true));
    }
    @Override
    public void start() throws Exception {
        log.info("Resource content handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void setup(TokenValidator tokenValidator, EventService eventService, AccessIndexService accessIndexService, String bucket, DeploymentServiceProvider deploymentServiceProvider) {
        this.deploymentServiceProvider = deploymentServiceProvider;
    }
    public boolean onEvent(Event event){
       return true;
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
}
