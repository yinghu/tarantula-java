package com.tarantula.cci;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.event.ResponsiveEvent;


public class ResourceEventHandler extends AbstractRequestHandler{

    private static TarantulaLogger log = JDKLogger.getLogger(ResourceEventHandler.class);

    private DeploymentServiceProvider deploymentServiceProvider;

    public ResourceEventHandler(){
        super(false);
    }
    public String name(){
        return RESOURCE_PATH;
    }
    public void onRequest(OnExchange exchange) throws Exception{
        log.warn("LOAD RESOURCE : 1");
        String path = exchange.path();
        log.warn("LOAD RESOURCE : "+path);
        //load js API in resources/web, public access
        Content _load = this.deploymentServiceProvider.resource(path.substring(1));
        exchange.onEvent(new ResponsiveEvent("",0,_load.data(),0,_load.type(),true));
     }
    @Override
    public void start() throws Exception {
        super.start();
        log.info("Resource content handler started");
    }


    public void setup(ServiceContext tcx){
        super.setup(tcx);
        this.deploymentServiceProvider = (DeploymentServiceProvider)tcx.serviceProvider(DeploymentServiceProvider.NAME);
    }
}
