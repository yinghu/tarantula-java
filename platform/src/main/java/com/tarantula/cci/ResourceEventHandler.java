package com.tarantula.cci;

import com.icodesoftware.Event;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.service.Metrics;


public class ResourceEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(ResourceEventHandler.class);


    private DeploymentServiceProvider deploymentServiceProvider;

    public ResourceEventHandler(){
    }
    public String name(){
        return "/resource";
    }
    public void onRequest(OnExchange exchange) throws Exception{
        String path = exchange.path();
        //load js API in resources/web, public access
        Content _load = this.deploymentServiceProvider.resource(path.substring(1));
        exchange.onEvent(new ResponsiveEvent("","",_load.data(),0,_load.type(),true));
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
    public boolean deployable(){return true;}
}
