package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.service.deployment.ApplicationQuery;
import com.tarantula.platform.service.deployment.LobbyQuery;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.SystemUtil;

public class AdminSetupModule implements Module {

    private ApplicationContext context;
    private DeploymentServiceProvider serviceProvider;
    private DataStore dataStore;
    private GsonBuilder builder;
    private AdminSetupObject dbObject;

    @Override
    public void onJoin(Session session) throws Exception {
        session.write(this.builder.create().toJson(dbObject).getBytes(),label());
    }

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        if(session.action().equals("applicationList")){
            OnAccess access = this.builder.create().fromJson(new String(payload),OnAccess.class);
            this.dbObject.list.clear();
            this.dataStore.list(new ApplicationQuery(access.accessId()),(a)->{
                this.dbObject.list.add(a);
                return true;
            });
            session.write(this.builder.create().toJson(this.dbObject).getBytes(),label());
        }
        else if(session.action().equals("lobbyList")){
            this.dbObject.list.clear();
            this.dataStore.list(new LobbyQuery(dataStore.bucket()),(a)->{
                this.dbObject.list.add(a);
                return true;
            });
            session.write(this.builder.create().toJson(this.dbObject).getBytes(),label());
        }
        else if(session.action().equals("addLobby")){
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.fromMap(SystemUtil.toMap(payload));
            desc.type("lobby");
            desc.category("game");
            desc.accessMode(Session.PROTECT_ACCESS_MODE);
            desc.deployCode(1);
            session.write(this.serviceProvider.createLobby(desc).getBytes(),this.label());
        }
        else if(session.action().equals("addApplication")){
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.fromMap(SystemUtil.toMap(payload));
            desc.type("application");
            desc.category("demo");
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
        this.dbObject = new AdminSetupObject(this.label());
        this.dbObject.name("setup tool");
        this.dataStore.list(new LobbyQuery(dataStore.bucket()),(a)->{
            this.context.log(a.name(),OnLog.INFO);
            this.dbObject.list.add(a);
            return true;
        });
        this.context.log("Admin setup module started", OnLog.INFO);
    }

    @Override
    public String label() {
        return "admin-setup";
    }
}
