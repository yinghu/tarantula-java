package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.icodesoftware.Event;
import com.icodesoftware.OnView;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.service.Metrics;
import com.tarantula.platform.util.OnViewSerializer;


public class ViewEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(ViewEventHandler.class);


    private DeploymentServiceProvider deploymentServiceProvider;

    private GsonBuilder builder;
    public ViewEventHandler(){
    }
    public String name(){
        return "/view";
    }
    public void onRequest(OnExchange exchange){
        String viewId = exchange.header(Session.TARANTULA_VIEW_ID);
        OnView onView = this.deploymentServiceProvider.onView(viewId);
        if(onView==null){
            onView = this.deploymentServiceProvider.onView(OnView.INVALID_VIEW_ID);
        }
        byte[] ret = this.builder.create().toJson(onView).getBytes();
        exchange.onEvent(new ResponsiveEvent("","",ret,0,"application/json","",true));
        deploymentServiceProvider.onUpdated(Metrics.REQUEST_COUNT,1);
    }
    @Override
    public void start() throws Exception {
        log.info("View content handler started");
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnView.class,new OnViewSerializer());
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
