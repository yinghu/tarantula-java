package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.*;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.TokenValidatorProvider;
import com.tarantula.platform.service.deployment.ApplicationQuery;
import com.tarantula.platform.service.deployment.LobbyQuery;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.ResponseSerializer;
import com.tarantula.platform.util.SystemUtil;

import java.util.concurrent.ConcurrentHashMap;

public class SudoRoleModule implements Module,Configuration.Listener {

    private ApplicationContext context;
    private DeploymentServiceProvider serviceProvider;
    private TokenValidatorProvider tokenValidatorProvider;
    private DataStore dataStore;
    private GsonBuilder builder;
    private ConcurrentHashMap<String,Configuration> cMap;
    @Override
    public void onJoin(Session session,OnUpdate onUpdate) throws Exception {
        session.write(this.builder.create().toJson(this._adminObjectOnLobby()).getBytes(),label());
    }

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        if(session.action().equals("applicationList")){
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            session.write(this.builder.create().toJson(this._adminObjectOnApplication(access.accessId())).getBytes(),label());
        }
        else if(session.action().equals("lobbyList")){
            session.write(this.builder.create().toJson(this._adminObjectOnLobby()).getBytes(),label());
        }
        else if(session.action().equals("addLobby")){
            LobbyDescriptor desc = new LobbyDescriptor();
            desc.fromMap(SystemUtil.toMap(payload));
            desc.type("lobby");
            desc.accessMode(Access.PROTECT_ACCESS_MODE);
            desc.deployCode(1);
            desc.tag(desc.typeId()+Recoverable.PATH_SEPARATOR+"lobby");
            session.write(this.serviceProvider.createLobby(desc).getBytes(),this.label());
        }
        else if(session.action().equals("addApplication")){
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.fromMap(SystemUtil.toMap(payload));
            desc.type("application");
            desc.deployPriority(10);
            desc.maxIdlesOnInstance(3);
            desc.maxInstancesPerPartition(100);
            desc.instancesOnStartupPerPartition(1);
            session.write(serviceProvider.createApplication(desc).getBytes(),this.label());
        }
        else if(session.action().equals("onLaunch")){//typeId
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            session.write(this.serviceProvider.launch(access.typeId()).getBytes(),label());
        }
        else if(session.action().equals("onShutdown")){//typeId
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            session.write(this.serviceProvider.shutdown(access.typeId()).getBytes(),label());
        }
        else if(session.action().equals("disableApplication")){//applicationId
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            session.write(this.serviceProvider.enableApplication(access.applicationId(),false).getBytes(),label());
        }
        else if(session.action().equals("enableApplication")){//applicationId
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            session.write(this.serviceProvider.enableApplication(access.applicationId(),true).getBytes(),label());
        }
        else if(session.action().equals("onReset")){//subtypeId
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            Descriptor desc = this.context.descriptor(access.applicationId());
            if(desc!=null&&desc.subtypeId()!=null&&(!desc.subtypeId().equals("lobby"))){
                desc.moduleArtifact((String) access.property("moduleArtifact"));
                desc.moduleVersion((String)access.property("moduleVersion"));
                desc.codebase((String)access.property("codebase"));
                session.write(this.serviceProvider.reset(desc).getBytes(),label());
            }
            else{
                session.write(payload,label());
            }
        }
        else if(session.action().equals("addModule")){
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.fromMap(SystemUtil.toMap(payload));
            String ret = this.serviceProvider.createModule(desc);
            this.context.log(ret,OnLog.INFO);
            session.write(this.builder.create().toJson(new AdminSetupObject("add module",label())).getBytes(),label());
        }
        else if(session.action().equals("listViews")){
            session.write(this.builder.create().toJson(new ResponseHeader(session.action(),"ok",true)).getBytes(),label());
        }
        else if(session.action().equals("listConfigs")){
            session.write(this.builder.create().toJson(new ResponseHeader(session.action(),"ok",true)).getBytes(),label());
        }
        else if(session.action().equals("deployView")){
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
            this.serviceProvider.deploy(onView);
            session.write(this.builder.create().toJson(new ResponseHeader(session.action(),"view deployed",true)).getBytes(),label());
        }
        else{
            session.write(payload,label());
        }
        return session.action().equals("onLeave");
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.cMap = new ConcurrentHashMap<>();
        this.serviceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.serviceProvider.registerConfigurationListener(this);
        this.tokenValidatorProvider = this.context.serviceProvider(TokenValidatorProvider.NAME);
        this.dataStore = this.context.dataStore(DeploymentServiceProvider.DEPLOY_DATA_STORE);
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.builder.registerTypeAdapter(AdminSetupObject.class,new AdminObjectSerializer());
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.context.log("Admin setup module started", OnLog.INFO);
    }
    @Override
    public String label() {
        return "admin-setup";
    }

    private AdminObject _adminObjectOnLobby(){
        AdminSetupObject ao = new AdminSetupObject("list lobby",label());
        ao.name("lobby list");
        this.dataStore.list(new LobbyQuery(dataStore.bucket()),(a)->{
            ao.list.add(a);
            return true;
        });
        return ao;
    }
    private AdminObject _adminObjectOnApplication(String lobbyId){
        AdminSetupObject ao = new AdminSetupObject("list app",label());
        ao.name("application list");
        ApplicationQuery aq = new ApplicationQuery(lobbyId);
        IndexSet iset = new IndexSet();
        iset.distributionKey(lobbyId);
        iset.label(aq.label());
        if(this.dataStore.load(iset)) {
            iset.keySet.forEach((s) -> {
                this.context.log("KEY->" + s, OnLog.INFO);
            });
            this.context.log("OUTPUT->"+new String(iset.toByteArray()),OnLog.INFO);
        }
        this.dataStore.list(aq,(a)->{
            ao.list.add(a);
            return true;
        });
        return ao;
    }

    @Override
    public void onConfiguration(Configuration c) {
        //this.context.log(c.distributionKey(),OnLog.WARN);
        //this.context.log(c.toString(),OnLog.WARN);
        cMap.put(c.distributionKey(),c);
    }
}
