package com.tarantula.admin;

import com.google.gson.*;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.*;
import com.tarantula.platform.presence.PermissionContext;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.SystemUtil;

import java.util.List;


public class SudoRoleModule implements Module {

    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;
    private AccessIndexService accessIndexService;

    private UserService userService;
    private GsonBuilder builder;

    @Override
    public boolean onRequest(Session session, byte[] payload) throws Exception {
        if(session.action().equals("onCheckPermission")){
            Access acc = userService.loadUser(session.systemId());
            session.write(new PermissionContext(acc.role(),true).toJson().toString().getBytes());
        }
        else if(session.action().equals("onEnablePresenceService")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String root = (String) onAccess.property("user");
            String password = (String) onAccess.property("password");
            String host = (String) onAccess.property("host");
            String suffix = (String) onAccess.property("suffix");
            String localPassword = (String) onAccess.property("localPassword");
            boolean suc = this.tokenValidatorProvider.enablePresenceService(root,password,suffix,host);
            if(suc){
                this.context.clusterProvider().deployService().enablePresenceService(root,password,suffix,host);
            }
            session.write(JsonUtil.toSimpleResponse(suc,suc?"remote presence service enabled on ["+host+"]":"failed").getBytes());
        }
        else if(session.action().equals("onDisablePresenceService")){
            this.tokenValidatorProvider.disablePresenceService(session.name());
            this.context.clusterProvider().deployService().disablePresenceService(session.name());
            session.write(JsonUtil.toSimpleResponse(true,"remote presence service disabled").getBytes());
        }
        else if(session.action().equals("onResetClusterKey")){
            boolean suc = this.tokenValidatorProvider.resetClusterKey();
            if(suc){
                this.context.clusterProvider().deployService().resetClusterKey();
            }
            session.write(JsonUtil.toSimpleResponse(suc,suc?"Cluster reset":"failed to reset key").getBytes());
        }
        else if(session.action().equals("onPresenceKey")){
            byte[] key = this.tokenValidatorProvider.clusterKey(session.name());
            PermissionContext permissionContext = new PermissionContext(key!=null?SystemUtil.toBase64String(key):null);
            session.write(permissionContext.toJson().toString().getBytes());
        }
        else if(session.action().equals("onCreateLabeledKey")){
            this.context.log(new String(payload),OnLog.WARN);
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String key = tokenValidatorProvider.createAccessKey(acc.typeId());
            PermissionContext pc = new PermissionContext(key);
            session.write(pc.toJson().toString().getBytes());
        }
        else if(session.action().equals("onTestLabeledKey")){
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            boolean suc = tokenValidatorProvider.validateAccessKey((String)acc.property(OnAccess.ACCESS_KEY))!=null;
            session.write(toMessage(suc?"key passed":"key failed",suc).toString().getBytes());
        }
        else if(session.action().equals("onStopAccessIndex")){
            accessIndexService.disable();
            session.write(toMessage(session.action(),true).toString().getBytes());
        }
        else if(session.action().equals("onStartAccessIndex")){
            accessIndexService.enable();
            session.write(toMessage(session.action(),true).toString().getBytes());
        }
        else if(session.action().equals("onFindUser")){
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String login = (String)acc.property(OnAccess.LOGIN);
            AccessIndex accessIndex = accessIndexService.get(login);
            if(accessIndex!=null){
                session.write(toMessage(accessIndex.distributionKey(),true).toString().getBytes());
            }else{
                session.write(toMessage("["+login+"] not found",false).toString().getBytes());
            }
        }

        else if(session.action().equals("onExportModule")){
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.codebase(acc.property(OnAccess.MODULE_CODE_BASE).toString());
            desc.moduleArtifact(acc.property(OnAccess.MODULE_ARTIFACT).toString());
            desc.moduleVersion(acc.property(OnAccess.MODULE_VERSION).toString());
            Response resp = this.deploymentServiceProvider.exportModule(desc);
            session.write(this.toMessage(resp.message(),resp.successful()).toString().getBytes());
        }
        else if(session.action().equals("onAddModule")){
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.codebase(acc.property(OnAccess.MODULE_CODE_BASE).toString());
            desc.moduleArtifact(acc.property(OnAccess.MODULE_ARTIFACT).toString());
            desc.moduleVersion(acc.property(OnAccess.MODULE_VERSION).toString());
            Response resp = this.deploymentServiceProvider.createModule(desc);
            session.write(this.toMessage(resp.message(),resp.successful()).toString().getBytes());
        }
        else if(session.action().equals("onLaunchModule")){//typeId
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            boolean suc = this.deploymentServiceProvider.launchModule(access.typeId());
            session.write(this.toMessage(suc?"module launched":"module not launched",suc).toString().getBytes());
        }
        else if(session.action().equals("onResetModule")){//typeId or moduleId
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            Descriptor desc = new DeploymentDescriptor();
            desc.typeId(access.typeId());
            desc.moduleArtifact((String) access.property(OnAccess.MODULE_ARTIFACT));
            desc.moduleVersion((String)access.property(OnAccess.MODULE_VERSION));
            desc.codebase((String)access.property(OnAccess.MODULE_CODE_BASE));
            AccessIndex index = this.accessIndexService.get(access.typeId());
            if(index!=null){
                desc.index(index.distributionKey());
            }
            boolean suc  = this.deploymentServiceProvider.resetModule(desc);
            session.write(this.toMessage(suc?"module rest":"module not reset",suc).toString().getBytes());
        }
        else if(session.action().equals("onShutdownModule")){//typeId
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            boolean suc = this.deploymentServiceProvider.shutdownModule(access.typeId());
            session.write(this.toMessage(suc?"module shutdown":"module not shutdown",suc).toString().getBytes());
        }
        else if(session.action().equals("onDeployView")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            OnView onView = new OnViewTrack();
            onView.owner(onAccess.typeId());//associated with a lobby type Id
            onView.viewId((String)onAccess.property("viewId"));
            String moduleContext = onAccess.property("deployUrl")!=null?(String) onAccess.property("deployUrl"):"root";
            if(moduleContext.startsWith("root")){
                int tix = moduleContext.lastIndexOf('/');
                if(tix<0){
                    onView.moduleResourceFile((String) onAccess.property("resourceName"));
                }
                else{
                    onView.moduleResourceFile(moduleContext.substring(tix+1)+"/"+onAccess.property("resourceName"));
                }
            }else{
                String rname = onAccess.property("deployUrl")+"/"+onAccess.property("resourceName");
                onView.moduleResourceFile(rname);
            }
            onView.moduleContext(moduleContext);
            Response suc = this.deploymentServiceProvider.createView(onView);
            session.write(toMessage(suc.message(),suc.successful()).toString().getBytes());
        }
        else if(session.action().equals("onDeployResource")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            Response suc = this.deploymentServiceProvider.deployResource((String)onAccess.property("deployUrl"),(String)onAccess.property("resourceName"));
            session.write(toMessage(suc.message(),suc.successful()).toString().getBytes());
        }
        else if(session.action().equals("onDeployModule")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            Response suc = this.deploymentServiceProvider.deployModule((String)onAccess.property("deployUrl"),(String)onAccess.property("resourceName"));
            session.write(toMessage(suc.message(),suc.successful()).toString().getBytes());
        }
        else if(session.action().equals("onMetrics")){
            Metrics metrics = context.metrics(Metrics.PERFORMANCE);
            MetricsContext adminContext = new MetricsContext();
            adminContext.metrics = metrics;
            session.write(adminContext.toJson().toString().getBytes());
        }
        else{
           throw new UnsupportedOperationException("operation ["+session.action()+"] not supported");
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.accessIndexService = this.context.serviceProvider(AccessIndexService.NAME);
        this.userService = this.context.serviceProvider(UserService.NAME);
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.context.log("Sudo setup module started", OnLog.INFO);
    }

    private JsonElement _parse(JsonParser parser, byte[] k,byte[] payload){
        try{
            return parser.parse(new String(payload));
        }catch (Exception ex){
            this.context.log("KEY->"+new String(k),OnLog.WARN);
            this.context.log("LOAD->"+new String(payload),OnLog.WARN);
            return new JsonObject();
        }
    }
    private JsonObject toMessage(String msg,boolean suc){
        JsonObject jms = new JsonObject();
        jms.addProperty("successful",suc);
        jms.addProperty("message",msg);
        return jms;
    }
    private JsonObject toJsonList(List<String> dataStoreList){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        JsonArray clist = new JsonArray();
        dataStoreList.forEach((d)->{
            clist.add(d);
        });
        jsonObject.add("list",clist);
        return jsonObject;
    }
}
