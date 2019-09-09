package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.DeploymentServiceProvider;

public class AdminDataStoreModule implements Module {

    private ApplicationContext context;
    private GsonBuilder builder;

    private DeploymentServiceProvider deploymentServiceProvider;
    public void onJoin(Session session,OnConnection onConnection) throws Exception{
         session.write(this.builder.create().toJson(_message("joined")).getBytes(),label());
    }

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        this.context.log(session.action(),OnLog.INFO);
        if(session.action().equals("backup")){
            DataStoreProvider dp = deploymentServiceProvider.dataStoreProvider();
            dp.backup(Distributable.DATA_SCOPE);
            dp.backup(Distributable.INTEGRATION_SCOPE);
            session.write(this.builder.create().toJson(_message("incremental backup data store")).getBytes(),label());
        }
        else if(session.action().equals("list")){
            AdminDataStoreObject dao = _message("count of data store list");
            dao.reset(this.context);
            session.write(this.builder.create().toJson(dao).getBytes(),label());
        }
        else{
            session.write(payload,label());
        }
        return session.action().equals("onLeave");
    }

    @Override
    public void setup(ApplicationContext context) throws Exception {
        this.context = context;
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(AdminDataStoreObject.class,new AdminObjectSerializer());
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.context.log("Admin data store module started", OnLog.INFO);
    }
    @Override
    public String label() {
        return "admin-data-store";
    }
    private AdminDataStoreObject _message(String message){
        return new AdminDataStoreObject(message,label());
    }
}
