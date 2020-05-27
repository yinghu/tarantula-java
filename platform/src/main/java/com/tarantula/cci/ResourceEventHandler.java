package com.tarantula.cci;

import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.Metrics;
import com.tarantula.platform.service.ServiceContext;


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
        //load js API in resources/web, public access
        byte[] _load = this.deploymentServiceProvider.resource(path.substring(1),null);
        exchange.onEvent(new ResponsiveEvent("","",_load,0,"text/javascript","",true));
        deploymentServiceProvider.onUpdated(Metrics.REQUEST_COUNT,1);
    }
    @Override
    public void start() throws Exception {
        log.info("Resource content handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }

    public void setup(ServiceContext tcx){
        this.deploymentServiceProvider = (DeploymentServiceProvider)tcx.serviceProvider(DeploymentServiceProvider.NAME);
    }
    public boolean onEvent(Event event){
       return true;
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
}
