package com.tarantula.cci;

import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.event.ResponsiveEvent;

import java.io.BufferedInputStream;

public class RootContentHandler implements RequestHandler {

    private static final JDKLogger log = JDKLogger.getLogger(RootContentHandler.class);
    private DeploymentServiceProvider deploymentServiceProvider;
    public String name(){
        return "/";
    }
    public void onRequest(OnExchange exchange){
        try{
            String path = exchange.path();
            log.info(path);
            if(path.equals("/")){
                path = "/tarantula.html";
            }
            byte[] _load;
            String contentType = "text/html";
            BufferedInputStream in = new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(path.substring(1)));
            try{
                _load = new byte[in.available()];
                in.read(_load);
            }catch (Exception ex){
                throw new RuntimeException(path);
            }
            finally {
                try{in.close();}catch (Exception exx){}
            }
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
        } catch (Exception exx){
            throw exx;
        }
    }
    public void setup(TokenValidator tokenValidator, EventService eventService, AccessIndexService accessIndexService, String bucket, DeploymentServiceProvider deploymentServiceProvider){
        this.deploymentServiceProvider = deploymentServiceProvider;
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
