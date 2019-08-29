package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.service.DataStoreProvider;

public class AdminDataStoreModule implements Module {

    private ApplicationContext context;
    private GsonBuilder builder;
    private AdminDataStoreObject dbObject;

    private DeploymentServiceProvider deploymentServiceProvider;
    public void onJoin(Session session) throws Exception{
         session.write(this.builder.create().toJson(dbObject).getBytes(),label());
    }

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        this.context.log(session.action(),OnLog.INFO);
        if(session.action().equals("onBackup")){
            DataStoreProvider dp = deploymentServiceProvider.dataStoreProvider();
            dp.backup(Distributable.DATA_SCOPE);
        }
        session.write(payload,label());
        return session.action().equals("onLeave");
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(AdminDataStoreObject.class,new AdminObjectSerializer());
        this.dbObject = new AdminDataStoreObject(this.label());
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.context.log("Admin data store module started", OnLog.INFO);
    }

    public void onTimer(OnUpdate update){
        update.on(this.builder.create().toJson(dbObject).getBytes());
    }
    @Override
    public String label() {
        return "admin-data-store";
    }
}
