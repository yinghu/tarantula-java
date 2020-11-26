package com.tarantula.cci;

import com.icodesoftware.Event;
import com.icodesoftware.OnView;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.service.Metrics;

public class RootContentHandler implements RequestHandler {

    private static final JDKLogger log = JDKLogger.getLogger(RootContentHandler.class);
    private DeploymentServiceProvider deploymentServiceProvider;
    //private OnView invalidView;
    public String name(){
        return "/";
    }
    public void onRequest(OnExchange exchange){//load from web folder including sub folders
        try{
            String path = exchange.path();
            if(path.equals("/")){
                path = "/index.html";
            }
            String contentType = "text/html";
            byte[] _load = this.deploymentServiceProvider.resource("root"+path,exchange.query());
            if(path.endsWith(".css")){
                contentType = "text/css";
            }
            else if(path.endsWith(".html")){
                contentType = "text/html";
            }
            else if(path.endsWith(".js")){
                contentType = "text/javascript";
            }
            else if(path.endsWith(".png")){
                contentType = "image/png";
            }
            else if(path.endsWith(".jpeg")){
                contentType = "image/jpeg";
            }
            else if(path.endsWith(".jpg")){
                contentType = "image/jpeg";
            }
            exchange.onEvent(new ResponsiveEvent("","",_load,0,contentType,"",true));
            deploymentServiceProvider.onUpdated(Metrics.REQUEST_COUNT,1);
        } catch (Exception exx){
            throw exx;
        }
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
        //this.invalidView = this.deploymentServiceProvider.onView(OnView.INVALID_VIEW_ID);
        log.info("Root content event handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }
}
