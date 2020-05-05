package com.tarantula.cci;

import com.tarantula.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.service.AccessIndexService;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.platform.service.TokenValidatorProvider;

import java.io.InputStream;

/**
 * Created by yinghu lu on 11/9/19.
 */
public class UploadEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(UploadEventHandler.class);


    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidator tokenValidator;
    public UploadEventHandler(){
    }
    public String name(){
        return "/upload";
    }
    public void onRequest(OnExchange exchange){
        try{
            String token = exchange.header(Session.TARANTULA_TOKEN);
            OnSession onSession = tokenValidator.validateToken(token);
            InputStream in = exchange.onStream();
            String path = exchange.path();
            log.warn(onSession.systemId()+" is uploading module ["+path+"]");
            String ret = this.deploymentServiceProvider.upload(in,path.substring(path.lastIndexOf("/")+1));
            exchange.onEvent(new ResponsiveEvent("","",ret.getBytes(),0,"text/html","",true));
        }catch (Exception ex){
            ex.printStackTrace();
            exchange.onError(ex,ex.getMessage());
        }
    }
    @Override
    public void start() throws Exception {
        log.info("Upload handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    public void setup(ServiceContext tcx){
        TokenValidatorProvider tp = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
        this.tokenValidator = tp.tokenValidator();
        this.deploymentServiceProvider = (DeploymentServiceProvider)tcx.serviceProvider(DeploymentServiceProvider.NAME);
    }
    public boolean onEvent(Event event){
       return true;
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }
}
