package com.tarantula.cci;

import com.icodesoftware.Event;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.service.Metrics;

public class RootContentHandler implements RequestHandler {

    private static final JDKLogger log = JDKLogger.getLogger(RootContentHandler.class);
    private DeploymentServiceProvider deploymentServiceProvider;
    public String name(){
        return ROOT_PATH;
    }
    public void onRequest(OnExchange exchange) throws Exception{//load from web folder including sub folders
        String path = exchange.path();
        if(path.equals("/")){
            path = "/index.html";
        }
        Content content = this.deploymentServiceProvider.resource("root"+path);
        byte[] _load = content.data();
        exchange.onEvent(new ResponsiveEvent("","",_load,0,content.type(),true));
        deploymentServiceProvider.onUpdated(Metrics.REQUEST_COUNT,1);
    }

    public void setup(ServiceContext tcx){
        this.deploymentServiceProvider = (DeploymentServiceProvider)tcx.serviceProvider(DeploymentServiceProvider.NAME);
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }

    @Override
    public void start() throws Exception {
        log.info("Root content event handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    public boolean deployable(){return true;}
}
