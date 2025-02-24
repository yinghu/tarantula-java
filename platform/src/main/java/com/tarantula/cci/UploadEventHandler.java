package com.tarantula.cci;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.GameCluster;
import com.icodesoftware.util.TRResponse;
import com.tarantula.platform.event.ResponsiveEvent;
import com.tarantula.platform.util.ResponseSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.util.concurrent.ConcurrentHashMap;

public class UploadEventHandler extends AbstractRequestHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(UploadEventHandler.class);
    private final static String METRICS_CATEGORY = "httpUploadCount";
    private DeploymentServiceProvider deploymentServiceProvider;

    private ConcurrentHashMap<Long,UploadContent> uMap = new ConcurrentHashMap<>();
    private DeployService deployService;
    private GsonBuilder builder;

    public UploadEventHandler(){
        super(true);
    }
    public String name(){
        return UPLOAD_PATH;
    }
    public void onRequest(OnExchange exchange) throws Exception{
        super.onRequest(exchange);
        String token = exchange.header(Session.TARANTULA_TOKEN);
        String typeId = exchange.header(Session.TARANTULA_TYPE_ID);
        OnSession onSession = tokenValidator.tokenValidator().validateToken(token);
        UploadContent uploadContent = new UploadContent(exchange.id(),typeId,exchange.onStream().readAllBytes());
        uMap.put(uploadContent.sessionId,uploadContent);
        if(typeId==null || typeId.equals("root")) {
            checkPermission(onSession,exchange.id(),"role/sudo");
        }
        else if(typeId.equals("createGameCluster")){
            checkPermission(onSession,exchange.id(),"role/admin");
        }
        else{
            checkPermission(onSession,exchange.id(),"role/admin");
        }
    }
    @Override
    public void start() throws Exception {
        super.start();
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(TRResponse.class,new ResponseSerializer());
        log.info("Upload handler started");
    }


    public void setup(ServiceContext tcx){
        super.setup(tcx);
        this.deploymentServiceProvider = tcx.deploymentServiceProvider();
        this.deployService = tcx.clusterProvider().deployService();
    }
    public boolean onEvent(Event event){
        JsonObject resp = JsonUtil.parse(event.payload());
        if(!resp.get("successful").getAsBoolean()){
            uMap.remove(event.sessionId());
            return super.onEvent(event);
        }
        OnExchange exchange = eMap.get(event.sessionId());
        UploadContent uploadContent = uMap.get(event.sessionId());
        String typeId = uploadContent.typeId;
        try {
            if (typeId == null || typeId.equals("root")) {
                String path = exchange.path();
                boolean suc = deployService.onUpload(path.substring(path.lastIndexOf("/") + 1), uploadContent.content);
                TRResponse response = new TRResponse("upload", suc ? "uploaded" : "failed", suc);
                return super.onEvent(new ResponsiveEvent("", event.sessionId(), this.builder.create().toJson(response).getBytes(), 0, "text/html", true));
            }
            if (typeId.equals("createGameCluster")) {
                String fn = SystemUtil.oid()+".png";
                boolean suc = deployService.onUpload("web/"+fn,uploadContent.content);
                return super.onEvent(new ResponsiveEvent("", event.sessionId(), JsonUtil.toSimpleResponse(suc,fn).getBytes(),0,"text/html",true));
            }
            String[] query = typeId.split("#");
            GameCluster gameCluster = deploymentServiceProvider.gameCluster(Long.parseLong(query[0]));
            if(gameCluster==null){
                throw new RuntimeException("no game cluster setup");
            }
            String gameClusterName = gameCluster.name();
            String path = exchange.path();
            String fn = gameClusterName + path.substring(path.lastIndexOf("/"));
            TokenValidatorProvider.AuthVendor authVendor = tokenValidator.authVendor(query[1]);
            if(authVendor != null){
                boolean suc = authVendor.upload(gameClusterName.toLowerCase()+"#"+fn, uploadContent.content);
                return super.onEvent(new ResponsiveEvent("", event.sessionId(), JsonUtil.toSimpleResponse(suc, fn).getBytes(), 0, "text/html", true));
            }else {
                fn = gameClusterName + "/" + SystemUtil.oid() + path.substring(path.lastIndexOf(".") - 1);
                boolean suc = deployService.onUpload("web/" + fn, uploadContent.content);
                return super.onEvent(new ResponsiveEvent("", event.sessionId(), JsonUtil.toSimpleResponse(suc, fn).getBytes(), 0, "text/html", true));
            }
        }catch (Exception ex){
            log.error("failed to upload context ["+typeId+"]",ex);
            return super.onEvent(new ResponsiveEvent("", event.sessionId(), JsonUtil.toSimpleResponse(false,ex.getMessage()).getBytes(),0,"text/html",true));
        }
    }

    public String metricsCategory(){
        return METRICS_CATEGORY;
    }


}
