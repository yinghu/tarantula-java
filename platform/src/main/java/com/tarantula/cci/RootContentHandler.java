package com.tarantula.cci;

import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.event.ResponsiveEvent;

public class RootContentHandler extends AbstractRequestHandler {

    private static final JDKLogger log = JDKLogger.getLogger(RootContentHandler.class);
    private final static String METRICS_CATEGORY = "httpRootCount";
    private DeploymentServiceProvider deploymentServiceProvider;

    public RootContentHandler(){
        super(false);
    }
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
        exchange.onEvent(new ResponsiveEvent("",0,_load,0,content.type(),true));
    }

    public void setup(ServiceContext tcx){
        super.setup(tcx);
        this.deploymentServiceProvider = (DeploymentServiceProvider)tcx.serviceProvider(DeploymentServiceProvider.NAME);
    }

    @Override
    public void start() throws Exception {
        super.start();
        log.info("Root content event handler started");
    }
    public String metricsCategory(){
        return METRICS_CATEGORY;
    }

}
