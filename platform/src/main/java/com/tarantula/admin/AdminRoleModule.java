package com.tarantula.admin;

import com.google.gson.GsonBuilder;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.presence.GameCluster;
import com.tarantula.platform.presence.UserAccount;

import com.tarantula.platform.service.DeploymentServiceProvider;
import com.tarantula.platform.util.OnAccessDeserializer;
import com.tarantula.platform.util.ResponseSerializer;

import java.util.ArrayList;


public class AdminRoleModule implements Module {

    private ApplicationContext context;
    private GsonBuilder builder;
    private DataStore account;
    private DeploymentServiceProvider deploymentServiceProvider;
    private int maxGameClusterCount;

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception {
        this.context.log(session.action(),OnLog.INFO);
        if(session.action().equals("onGameClusterList")){
            AdminContext adminContext = new AdminContext();
            adminContext.gameClusterList = new ArrayList<>();
            IndexSet idx = new IndexSet();
            idx.distributionKey(session.systemId());
            idx.label(Account.GameClusterLabel);
            if(account.load(idx)){
                idx.keySet.forEach((k)->{
                    GameCluster g = this.deploymentServiceProvider.gameCluster(k);
                    if(g!=null){
                        adminContext.gameClusterList.add(g);
                    }
                });
            }
            session.write(adminContext.toJson().toString().getBytes(),label());
        }
        else if(session.action().equals("onStatistics")){
            Statistics statistics = this.deploymentServiceProvider.statistics();
            statistics.summary((e)->{
            });
            session.write(this.builder.create().toJson(new ResponseHeader(session.action(),"load statistics",true)).getBytes(),label());
        }
        else if(session.action().equals("onCreateGameCluster")){
            Account acc = new UserAccount();
            acc.distributionKey(session.systemId());
            if(account.load(acc)&&acc.gameClusterCount(0)<maxGameClusterCount){
                OnAccess onAccess = this.builder.create().fromJson(new String(payload).trim(),OnAccess.class);
                GameCluster gc = this.deploymentServiceProvider.createGameCluster(onAccess.name(),"basic");
                if(gc.successful()){
                    IndexSet idx = new IndexSet();
                    idx.distributionKey(acc.distributionKey());
                    idx.label(Account.GameClusterLabel);
                    idx.keySet.add(gc.distributionKey());
                    if(!account.createIfAbsent(idx,true)){
                        idx.keySet.add(gc.distributionKey());//update on existing
                        account.update(idx);
                    }
                    //idx.keySet.forEach((k)->{
                       // this.context.log("KEY->"+k,OnLog.WARN);
                    //});
                    acc.gameClusterCount(1);
                    account.update(acc);
                }
                session.write(this.builder.create().toJson(new ResponseHeader(session.action(),gc.message(),gc.successful())).getBytes(),label());
            }
            else{
                //reach max count
                session.write(this.builder.create().toJson(new ResponseHeader(session.action(),"you already have max game clusters",false)).getBytes(),label());
            }
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
        this.builder.registerTypeAdapter(ResponseHeader.class,new ResponseSerializer());
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.account = this.context.dataStore(Account.DataStore);
        this.deploymentServiceProvider = this.context.serviceProvider(DeploymentServiceProvider.NAME);
        this.maxGameClusterCount = Integer.parseInt(this.context.configuration("setup").property("maxGameClusterCount"));
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
