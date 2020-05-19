package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.presence.GameCluster;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.service.deployment.LobbyConfiguration;
import com.tarantula.platform.service.deployment.XMLParser;

import java.util.List;

public class AdminRoleModule implements Module {

    private ApplicationContext context;
    private GsonBuilder builder;
    private DataStore account;
    private DeploymentServiceProvider deploymentServiceProvider;
    private int maxGameClusterCount;
    private List<LobbyConfiguration> lbs;
    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        this.context.log(session.action(),OnLog.INFO);
         if(session.action().equals("onCreateGameCluster")){
            OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
            GameCluster gc = new GameCluster();
            gc.typeId(onAccess.name());
            gc.name(onAccess.name());

            //gc.description(onAccess.name());
            //gc.singleton(true);
            //String ret = this.deploymentServiceProvider.createGameCluster(gc);
            ResponseHeader resp = new ResponseHeader(session.action(),onAccess.name(),true);
            session.write(this.builder.create().toJson(resp).getBytes(),label());
        }
        /**
        else if(session.action().equals("backup")){
            DataStoreProvider dp = deploymentServiceProvider.dataStoreProvider();
            dp.backup(Distributable.DATA_SCOPE);
            dp.backup(Distributable.INTEGRATION_SCOPE);
            session.write(this.builder.create().toJson(_message("incremental backup data store")).getBytes(),label());
        }
        else if(session.action().equals("list")){
            AdminDataStoreObject dao = _message("count of data store list");
            dao.reset(this.context);
            session.write(this.builder.create().toJson(dao).getBytes(),label());
        }**/
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
        this.account = this.context.dataStore(Account.DataStore);
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.maxGameClusterCount = Integer.parseInt(this.context.configuration("setup").property("maxGameClusterCount"));
        XMLParser xml = new XMLParser();
        xml.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("game-cluster-singleton.xml"));
        this.lbs = xml.configurations;
        this.context.log("Admin role module started with max game cluster count ["+maxGameClusterCount+"]", OnLog.INFO);
    }
    @Override
    public String label() {
        return "admin-role";
    }
    private AdminDataStoreObject _message(String message){
        return new AdminDataStoreObject(message,label());
    }
}
