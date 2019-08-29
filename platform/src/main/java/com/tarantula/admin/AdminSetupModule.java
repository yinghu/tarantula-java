package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.service.deployment.ApplicationQuery;
import com.tarantula.platform.service.deployment.LobbyQuery;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.SystemUtil;

public class AdminSetupModule implements Module {

    private ApplicationContext context;
    private DeploymentServiceProvider serviceProvider;
    private DataStore dataStore;
    private GsonBuilder builder;

    @Override
    public void onJoin(Session session) throws Exception {
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
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.fromMap(SystemUtil.toMap(payload));
            desc.type("lobby");
            desc.subtypeId(desc.typeId()+"-lobby");
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
                desc.moduleArtifact(access.header("moduleArtifact"));
                desc.moduleVersion(access.header("moduleVersion"));
                desc.codebase(access.header("codebase"));
                session.write(this.serviceProvider.reset(desc).getBytes(),label());
            }
            else{
                session.write(payload,label());
            }
        }
        else{
            session.write(payload,label());
        }
        return session.action().equals("onLeave");
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.serviceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.dataStore = this.context.dataStore(DeploymentServiceProvider.DEPLOY_DATA_STORE);
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.builder.registerTypeAdapter(AdminSetupObject.class,new AdminObjectSerializer());
        this.context.log("Admin setup module started", OnLog.INFO);
    }
    @Override
    public String label() {
        return "admin-setup";
    }

    private AdminObject _adminObjectOnLobby(){
        AdminSetupObject ao = new AdminSetupObject(label());
        ao.name("lobby list");
        this.dataStore.list(new LobbyQuery(dataStore.bucket()),(a)->{
            ao.list.add(a);
            return true;
        });
        return ao;
    }
    private AdminObject _adminObjectOnApplication(String lobbyId){
        AdminSetupObject ao = new AdminSetupObject(label());
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
}
