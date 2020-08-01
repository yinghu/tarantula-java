package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.*;
import com.tarantula.platform.presence.Membership;
import com.tarantula.platform.presence.PermissionContext;
import com.tarantula.platform.presence.SubscriptionFee;
import com.tarantula.platform.presence.User;

import com.tarantula.platform.service.AccessIndexService;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.TokenValidatorProvider;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
            session.write(new PermissionContext(acc.role(),true).toJson().toString().getBytes(),label());
        }
        else if(session.action().equals("onDataStoreCountList")){
            JsonObject jm = this.toDataStoreCount();
            session.write(jm.toString().getBytes(),label());
        }
        else if(session.action().equals("onCreateLabeledKey")){
            String key = tokenValidatorProvider.accessKey("websocket");
            PermissionContext pc = new PermissionContext(key);
            session.write(pc.toJson().toString().getBytes(),label());
        }
        else if(session.action().equals("onTestLabeledKey")){
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            boolean suc = tokenValidatorProvider.validateAccessKey((String)acc.property(OnAccess.ACCESS_KEY));
            session.write(toMessage(suc?"key passed":"key failed",suc).toString().getBytes(),label());
        }
        else if(session.action().equals("onStopAccessIndex")){
            accessIndexService.update(false);
            session.write(toMessage(session.action(),true).toString().getBytes(),label());
        }
        else if(session.action().equals("onStartAccessIndex")){
            accessIndexService.update(true);
            session.write(toMessage(session.action(),true).toString().getBytes(),label());
        }
        else if(session.action().equals("onFindUser")){
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            String login = (String)acc.property(OnAccess.LOGIN);
            AccessIndex accessIndex = accessIndexService.get(login);
            if(accessIndex!=null){
                session.write(toMessage(accessIndex.distributionKey(),true).toString().getBytes(),login);
            }else{
                session.write(toMessage("["+login+"] not found",false).toString().getBytes(),label());
            }
        }
        else if(session.action().equals("onSubscriptionList")){
            DataStore mds = this.context.dataStore(Subscription.DataStore);
            SubscriptionContext sct = new SubscriptionContext();
            sct.subscriptionList = new ArrayList<>();
            //mds.traverse((d,o,k,v)->{
                //Subscription sub = new Membership();
                //sub.distributionKey(new String(k));
                //sub.fromMap(SystemUtil.toMap(v));
                //sct.subscriptionList.add(sub);
                //return true;
            //});
            session.write(sct.toJson().toString().getBytes(),this.label());
        }
        else if(session.action().equals("onBackupDataStore")){
            this.deploymentServiceProvider.issueDataStoreBackup();
            session.write(toMessage("backup commnad issued",true).toString().getBytes(),label());
        }
        else if(session.action().equals("onFindDataStore")){
            session.write(toMessage("find data store",true).toString().getBytes(),label());
        }

        else if(session.action().equals("onAddModule")){
            OnAccess acc = this.builder.create().fromJson(new String(payload),OnAccess.class);
            this.context.log(acc.property(OnAccess.MODULE_CODE_BASE).toString(),OnLog.WARN);
            this.context.log(acc.property(OnAccess.MODULE_ARTIFACT).toString(),OnLog.WARN);
            this.context.log(acc.property(OnAccess.MODULE_VERSION).toString(),OnLog.WARN);
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.codebase(acc.property(OnAccess.MODULE_CODE_BASE).toString());
            desc.moduleArtifact(acc.property(OnAccess.MODULE_ARTIFACT).toString());
            desc.moduleVersion(acc.property(OnAccess.MODULE_VERSION).toString());
            boolean suc = this.deploymentServiceProvider.createModule(desc);
            session.write(this.toMessage(suc?"module added":"module not added",suc).toString().getBytes(),label());
        }
        else if(session.action().equals("onLaunchModule")){//typeId
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            boolean suc = this.deploymentServiceProvider.launchModule(access.typeId());
            session.write(this.toMessage(suc?"module launched":"module not launched",suc).toString().getBytes(),label());
        }
        else if(session.action().equals("onResetModule")){//subtypeId
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            Descriptor desc = new DeploymentDescriptor();
            desc.typeId(access.typeId());
            desc.moduleArtifact((String) access.property(OnAccess.MODULE_ARTIFACT));
            desc.moduleVersion((String)access.property(OnAccess.MODULE_VERSION));
            desc.codebase((String)access.property(OnAccess.MODULE_CODE_BASE));
            boolean suc  = this.deploymentServiceProvider.resetModule(desc);
            session.write(this.toMessage(suc?"module rest":"module not reset",suc).toString().getBytes(),label());
        }
        else if(session.action().equals("onShutdownModule")){//typeId
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            boolean suc = this.deploymentServiceProvider.shutdownModule(access.typeId());
            session.write(this.toMessage(suc?"module shutdown":"module not shutdown",suc).toString().getBytes(),label());
        }
        else if(session.action().equals("onConfigurationList")){
            List<Configuration> configurationList = this.deploymentServiceProvider.configuration();
            configurationList.forEach((c)->this.deploymentServiceProvider.update(c));
            session.write(this.builder.create().toJson(new ResponseHeader(session.action(),"ok",true)).getBytes(),label());
        }
        else if(session.action().equals("onDeployView")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
            OnView onView = new OnViewTrack();
            onView.owner(onAccess.typeId());//associated with a lobby type Id
            onView.viewId((String)onAccess.property("viewId"));
            if(onAccess.property("deployUrl").equals("root")){
                onView.moduleResourceFile((String) onAccess.property("resourceName"));
            }else{
                String rname = onAccess.property("deployUrl")+"/"+onAccess.property("resourceName");
                onView.moduleResourceFile(rname);
            }
            onView.contentBaseUrl((String) onAccess.property("deployUrl"));
            this.deploymentServiceProvider.deploy(onView);
            session.write(toMessage("view deployed",true).toString().getBytes(),label());
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
    @Override
    public String label() {
        return "admin-setup";
    }

    private JsonObject toMessage(String msg,boolean suc){
        JsonObject jms = new JsonObject();
        jms.addProperty("successful",suc);
        jms.addProperty("message",msg);
        return jms;
    }
    private JsonObject toDataStoreCount(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",true);
        long cnt =0;
        //for(int i=0;i<this.deploymentServiceProvider.clusterPartitionCount();i++){
            //cnt += this.context.dataStore("p_"+i).count();
        //}
        jsonObject.addProperty("AccessIndex",cnt);
        jsonObject.addProperty("User",this.context.dataStore(Access.DataStore).count());
        jsonObject.addProperty("Session",this.context.dataStore(OnSession.DataStore).count());
        jsonObject.addProperty("Account",this.context.dataStore(Account.DataStore).count());
        jsonObject.addProperty("Presence",this.context.dataStore(Presence.DataStore).count());
        jsonObject.addProperty("Subscription",this.context.dataStore(Subscription.DataStore).count());
        jsonObject.addProperty("Purchase",this.context.dataStore(SubscriptionFee.DataStore).count());
        jsonObject.addProperty("System",this.context.dataStore(DeploymentServiceProvider.DEPLOY_DATA_STORE).count());
        return jsonObject;
    }
}
