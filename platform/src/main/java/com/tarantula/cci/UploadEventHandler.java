package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.AccessControl;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.util.ResponseSerializer;

import java.io.InputStream;

public class UploadEventHandler implements RequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(UploadEventHandler.class);

    private DeploymentServiceProvider deploymentServiceProvider;
    private DeployService deployService;
    private TokenValidatorProvider tokenValidator;
    private GsonBuilder builder;

    public UploadEventHandler(){
    }
    public String name(){
        return UPLOAD_PATH;
    }
    public void onRequest(OnExchange exchange) throws Exception{
        String token = exchange.header(Session.TARANTULA_TOKEN);
        String typeId = exchange.header(Session.TARANTULA_TYPE_ID);
        OnSession onSession = tokenValidator.tokenValidator().validateToken(token);
        if(typeId==null || typeId.equals("root")) {
            if (tokenValidator.role(onSession.systemId()).accessControl() < AccessControl.root.accessControl()) {
                throw new RuntimeException("no access permission");
            }
            InputStream in = exchange.onStream();
            String path = exchange.path();
            //log.warn(onSession.systemId() + " is uploading module [" + path + "]");
            boolean suc = deployService.upload(path.substring(path.lastIndexOf("/") + 1), in.readAllBytes());
            ResponseHeader resp = new ResponseHeader("upload", suc ? "uploaded" : "failed", suc);
            exchange.onEvent(new ResponsiveEvent("", "", this.builder.create().toJson(resp).getBytes(), 0, "text/html", true));
        }
        else{
            if (tokenValidator.role(onSession.systemId()).accessControl() < AccessControl.admin.accessControl()) {
                throw new RuntimeException("no access permission");
            }
            GameCluster gameCluster = deploymentServiceProvider.gameCluster(typeId);
            if(gameCluster==null){
                throw new RuntimeException("no game cluster setup");
            }
            String gameClusterName = (String) gameCluster.property(GameCluster.NAME);
            String path = exchange.path();
            InputStream in = exchange.onStream();
            byte[] data = in.readAllBytes();
            String fn = gameClusterName+ path.substring(path.lastIndexOf("/"));
            //log.warn(onSession.systemId() + " is uploading file [" + fn + "] to ["+gameClusterName+"] from ["+path+"]");
            boolean suc = deployService.upload("web/"+fn,data);
            exchange.onEvent(new ResponsiveEvent("","", JsonUtil.toSimpleResponse(suc,fn).getBytes(),0,"text/html",true));
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
        this.deploymentServiceProvider = tcx.deploymentServiceProvider();
        this.tokenValidator = (TokenValidatorProvider) tcx.serviceProvider(TokenValidatorProvider.NAME);
        this.deployService = tcx.clusterProvider(Distributable.INTEGRATION_SCOPE).deployService();
    }
    public boolean onEvent(Event event){
       return true;
    }
    public void onCheck(){
        //log.warn("Total active session ["+_hex.size()+"] on ["+name()+"]");
    }

}
