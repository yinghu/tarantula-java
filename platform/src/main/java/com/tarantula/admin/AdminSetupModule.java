package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.service.deployment.LobbyQuery;
import com.tarantula.platform.util.SystemUtil;

public class AdminSetupModule implements Module {

    private ApplicationContext context;
    private DeploymentServiceProvider serviceProvider;
    private DataStore dataStore;
    private GsonBuilder builder;
    private AdminObject dbObject;

    @Override
    public void onJoin(Session session) throws Exception {
        session.write(this.builder.create().toJson(dbObject.setup()).getBytes(),label());
    }

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        if(session.action().equals("addLobby")){
            DeploymentServiceProvider dps = this.context.serviceProvider(DeploymentServiceProvider.NAME);
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.fromMap(SystemUtil.toMap(payload));
            desc.type("lobby");
            desc.category("game");
            desc.accessMode(Session.PROTECT_ACCESS_MODE);
            desc.deployCode(1);
            session.write(dps.createLobby(desc).getBytes(),this.label());
        }
        else if(session.action().equals("addApplication")){
            DeploymentServiceProvider dps = this.context.serviceProvider(DeploymentServiceProvider.NAME);
            DeploymentDescriptor desc = new DeploymentDescriptor();
            desc.fromMap(SystemUtil.toMap(payload));
            desc.type("application");
            desc.category("demo");
            desc.deployPriority(10);
            desc.maxIdlesOnInstance(3);
            desc.maxInstancesPerPartition(100);
            desc.instancesOnStartupPerPartition(1);
            session.write(dps.createApplication(desc).getBytes(),this.label());
        }
        else if(session.action().equals("onReset")){
            session.write(payload,label());
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
        this.builder.registerTypeAdapter(AdminObject.class,new AdminObjectSerializer());
        this.dbObject = new AdminObject();
        this.dbObject.successful(true);
        this.dbObject.name("setup tool");
        this.dataStore.list(new LobbyQuery(dataStore.bucket()),(a)->{
            this.context.log(a.name(),OnLog.INFO);
            return true;
        });
        this.context.log("Admin setup module started", OnLog.INFO);
    }

    @Override
    public String label() {
        return "admin";
    }
}
