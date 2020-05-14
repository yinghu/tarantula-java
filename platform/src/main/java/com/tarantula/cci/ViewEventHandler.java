package com.tarantula.cci;

import com.tarantula.Event;
import com.tarantula.OnView;
import com.tarantula.TarantulaLogger;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.ServiceContext;


public class ViewEventHandler implements RequestHandler, OnView.Listener {

    private static TarantulaLogger log = JDKLogger.getLogger(ViewEventHandler.class);


    private DeploymentServiceProvider deploymentServiceProvider;

    public ViewEventHandler(){
    }
    public String name(){
        return "/view";
    }
    public void onRequest(OnExchange exchange){
        String path = exchange.path();
        //load js API in resources/web, public access
        byte[] _load = this.deploymentServiceProvider.resource(path.substring(1),null);
        exchange.onEvent(new ResponsiveEvent("","",_load,0,"text/javascript","",true));
    }
    @Override
    public void start() throws Exception {
        log.info("Resource content handler started");
        this.deploymentServiceProvider.registerOnViewListener(this);
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

    @Override
    public void onView(OnView onView) {
        log.warn(onView.viewId()+">>"+onView.toString());
    }
}
