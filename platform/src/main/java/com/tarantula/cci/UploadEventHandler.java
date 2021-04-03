package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.service.DeployService;
import com.icodesoftware.service.RecoverService;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.AccessControl;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.util.ResponseSerializer;

import java.io.InputStream;

/**
 * Updated by yinghu lu on 7/26/20.
 */
public class UploadEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(UploadEventHandler.class);

    private DeployService deployService;
    private RecoverService recoverService;
    private TokenValidator tokenValidator;
    private GsonBuilder builder;

    public UploadEventHandler(){
    }
    public String name(){
        return "/upload";
    }
    public void onRequest(OnExchange exchange){
        try{
            String token = exchange.header(Session.TARANTULA_TOKEN);
            OnSession onSession = tokenValidator.validateToken(token);
            if(!recoverService.checkAccessControl(onSession.systemId(),AccessControl.root)){
                throw new RuntimeException("no access permission");
            }
            InputStream in = exchange.onStream();
            String path = exchange.path();
            log.warn(onSession.systemId() + " is uploading module [" + path + "]");
            boolean suc = deployService.upload(path.substring(path.lastIndexOf("/") + 1),in.readAllBytes());
            ResponseHeader resp = new ResponseHeader("upload",suc?"uploaded":"failed",suc);
            exchange.onEvent(new ResponsiveEvent("", "",this.builder.create().toJson(resp).getBytes(), 0, "text/html", true));
            //}
            //else{
                //ResponseHeader resp = new ResponseHeader("upload","no permission operation",true);
                //exchange.onEvent(new ResponsiveEvent("", "", this.builder.create().toJson(resp).getBytes(), 0, "text/html", "", true));
            //}
        }catch (Exception ex){
            ex.printStackTrace();
            exchange.onError(ex,ex.getMessage());
        }
    }
    @Override
    public void start() throws Exception {
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        log.info("Upload handler started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    public void setup(ServiceContext tcx){
        TokenValidatorProvider tokenValidatorProvider = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
        this.tokenValidator = tokenValidatorProvider.tokenValidator();
        this.deployService = tcx.clusterProvider(Distributable.INTEGRATION_SCOPE).deployService();
        this.recoverService = tcx.clusterProvider(Distributable.INTEGRATION_SCOPE).recoverService();
    }
    public boolean onEvent(Event event){
       return true;
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }

}
