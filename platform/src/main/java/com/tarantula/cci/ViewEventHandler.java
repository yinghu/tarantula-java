package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.tarantula.Event;
import com.tarantula.OnView;
import com.tarantula.Session;
import com.tarantula.TarantulaLogger;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.platform.util.OnViewSerializer;

import java.util.concurrent.ConcurrentHashMap;


public class ViewEventHandler implements RequestHandler, OnView.Listener {

    private static TarantulaLogger log = JDKLogger.getLogger(ViewEventHandler.class);


    private DeploymentServiceProvider deploymentServiceProvider;

    private ConcurrentHashMap<String,OnView> _vMap;
    private GsonBuilder builder;
    public ViewEventHandler(){
    }
    public String name(){
        return "/view";
    }
    public void onRequest(OnExchange exchange){
        String viewId = exchange.header(Session.TARANTULA_VIEW_ID);
        OnView onView = _vMap.get(viewId);
        if(onView==null){
            onView = _vMap.get("invalid.request");
        }
        byte[] ret = this.builder.create().toJson(onView).getBytes();
        exchange.onEvent(new ResponsiveEvent("","",ret,0,"application/json","",true));
    }
    @Override
    public void start() throws Exception {
        log.info("View content handler started");
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnView.class,new OnViewSerializer());
        this._vMap = new ConcurrentHashMap<>();
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
        //log.warn(onView.viewId()+">>"+onView.toString());
        _vMap.put(onView.viewId(),onView);
    }
}
