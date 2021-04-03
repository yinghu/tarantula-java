package com.tarantula.admin;

import com.google.gson.*;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.platform.*;
import com.tarantula.platform.presence.PermissionContext;
import com.tarantula.platform.presence.SubscriptionFee;
import com.tarantula.platform.presence.User;

import com.tarantula.platform.util.OnAccessDeserializer;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.sun.tools.attach.*;

public class SudoRoleModule implements Module {

    private ApplicationContext context;
    private DeploymentServiceProvider deploymentServiceProvider;
    private TokenValidatorProvider tokenValidatorProvider;
    private AccessIndexService accessIndexService;
    //private DataStore dataStore;
    private GsonBuilder builder;
    private ConcurrentHashMap<String,Configuration> cMap;
    private DataStore uDatastore;

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        if(session.action().equals("onCheckPermission")){
            User acc = new User();
            acc.distributionKey(session.systemId());
            uDatastore.load(acc);
            session.write(new PermissionContext(acc.role(),true).toJson().toString().getBytes());
        }
        else if(session.action().equals("onCreateLabeledKey")){
            this.context.log(new String(payload),OnLog.WARN);
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String key = tokenValidatorProvider.accessKey(acc.typeId());
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
        else if(session.action().equals("onConfigurationList")){
            List<Configuration> configurationList = this.deploymentServiceProvider.configuration();
            session.write(toJson(configurationList).toString().getBytes());
        }
        else if(session.action().equals("onUpdateConfiguration")){
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            Configuration configuration = new ApplicationConfiguration();
            configuration.distributionKey(access.property(OnAccess.ACCESS_ID).toString());
            Map<String,Object> _payload = access.toMap();
            _payload.remove(OnAccess.ACCESS_ID);
            _payload.remove(OnAccess.COMMAND);
            _payload.remove(OnAccess.SERVICE_TAG);
            configuration.fromMap(_payload);
            boolean suc = this.context.dataStore(DeploymentServiceProvider.DEPLOY_DATA_STORE).update(configuration);
            this.deploymentServiceProvider.configure(configuration.distributionKey());
            session.write(toMessage("configuration updated ["+configuration.distributionKey()+"]",suc).toString().getBytes());
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
        else if(session.action().equals("onListDataStore")){
            List<String> dlist = this.deploymentServiceProvider.listDataStore();
            session.write(toJsonList(dlist).toString().getBytes());
        }
        else if(session.action().equals("onLoadDataStore")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String dataStore = onAccess.property("dataStore").toString();
            if(this.deploymentServiceProvider.validDataStore(dataStore)){
                DataStore ds = this.context.dataStore(dataStore);
                JsonArray list = new JsonArray();
                JsonParser parser = new JsonParser();
                ds.backup().list((k,v)->{
                    JsonObject r = new JsonObject();
                    r.addProperty("id",new String(k));
                    r.add("payload",_parse(parser,k,v));
                    list.add(r);
                    return true;
                });
                JsonObject ret = new JsonObject();
                ret.add("resultRet",list);
                session.write(ret.toString().getBytes());
            }else{
                session.write(toMessage("data store not existed->"+dataStore,false).toString().getBytes());
            }

        }
        else if(session.action().equals("onBackupDataStore")){
            this.deploymentServiceProvider.issueDataStoreBackup();
            session.write(toMessage("backup commnad issued",true).toString().getBytes());
        }
        else{
           throw new UnsupportedOperationException("operation ["+session.action()+"] not supported");
        }
        return false;
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.cMap = new ConcurrentHashMap<>();
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.accessIndexService = this.context.serviceProvider(AccessIndexService.NAME);
        this.uDatastore = this.context.dataStore(Access.DataStore);
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.context.log("Admin setup module started", OnLog.INFO);
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
    private JsonObject toJson(List<Configuration> configurations){
        JsonObject jsonObject = new JsonObject();
        JsonArray clist = new JsonArray();
        configurations.forEach(configuration ->{
            JsonObject jc = new JsonObject();
            jc.addProperty("accessId",configuration.distributionKey());
            jc.addProperty("type",configuration.type());
            configuration.properties().forEach((p)->{
                jc.addProperty(p.name(),p.value());
            });
            clist.add(jc);
        });
        jsonObject.add("configurations",clist);
        return jsonObject;
    }
}
