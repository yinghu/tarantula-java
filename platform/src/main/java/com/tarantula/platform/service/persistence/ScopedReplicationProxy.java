package com.tarantula.platform.service.persistence;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hazelcast.nio.serialization.Portable;
import com.icodesoftware.*;

import com.icodesoftware.lmdb.TransactionLog;
import com.icodesoftware.lmdb.TransactionLogManager;
import com.icodesoftware.service.*;
import com.icodesoftware.util.ScheduleRunner;
import com.tarantula.platform.event.TransactionReplicationEvent;
import com.tarantula.platform.service.cluster.DistributionReplicator;

import java.util.ArrayList;
import java.util.List;

public class ScopedReplicationProxy implements MapStoreListener,ServiceProvider{

    private final static String CONFIG = "replication-service-settings";

    protected ServiceContext serviceContext;

    protected TarantulaLogger logger;

    private final String name;
    protected final int scope;
    protected boolean asyncDistributing = true;
    protected boolean broadcasting = true;
    protected long asyncInterval = 100;

    protected int maxReplicationNodes = 3;

    protected TransactionLogManager transactionLogManager;
    protected DistributionReplicator distributionReplicator;

    public ScopedReplicationProxy(String name,int scope){
        this.name = name;
        this.scope = scope;
        transactionLogManager = new TransactionLogManager();
    }

    public void onUpdating(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId){
        transactionLogManager.onUpdating(metadata,key,value,transactionId);
    }
    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer buffer){
        return transactionLogManager.onRecovering(metadata,key,buffer);
    }

    public boolean onRecovering(Metadata metadata,Recoverable.DataBuffer key,DataStore.BufferStream bufferStream){
        return transactionLogManager.onRecovering(metadata,key,bufferStream);
    }
    @Override
    public boolean onDeleting(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId) {
        return transactionLogManager.onDeleting(metadata,key,value,transactionId);
    }

    @Override
    public void onCommit(int scope,long transactionId) {
        transactionLogManager.onCommit(scope,transactionId);
    }

    @Override
    public void onAbort(int scope,long transactionId) {
        transactionLogManager.onAbort(scope,transactionId);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        transactionLogManager.setup(serviceContext);
        Configuration configuration = serviceContext.configuration(CONFIG);
        JsonObject conf = ((JsonElement)configuration.property(name)).getAsJsonObject();
        if(conf==null) {
            logger.warn("Using default replication setting ["+asyncDistributing+" : "+asyncInterval+" : "+broadcasting+" : "+maxReplicationNodes+"]");
            return;
        }
        asyncDistributing = conf.get("asyncDistributing").getAsBoolean();
        broadcasting = conf.get("broadcasting").getAsBoolean();
        asyncInterval = conf.get("asyncInterval").getAsLong();
        maxReplicationNodes = conf.get("maxReplicationNodes").getAsInt();
        logger.warn("Using replication setting ["+asyncDistributing+" : "+asyncInterval+" : "+broadcasting+" : "+maxReplicationNodes+"]");
    }

    @Override
    public void waitForData() {
        if(scope== Distributable.INTEGRATION_SCOPE){
            distributionReplicator = (DistributionReplicator) serviceContext.clusterProvider().accessIndexService();
        }
        else if(scope==Distributable.DATA_SCOPE){
            distributionReplicator = (DistributionReplicator)serviceContext.clusterProvider().recoverService();
        }
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        transactionLogManager.close();
    }

    public void onTransactionReplicationEvent(TransactionReplicationEvent event){
        List<TransactionLog> logs = new ArrayList<>();
        for(Portable portableTransactionLog : event.pendingLogs){
            logs.add(((PortableTransactionLog)portableTransactionLog).transactionLog);
        }
        transactionLogManager.onTransaction(logs);
    }

    public TransactionLogManager transactionLogManager(){
        return transactionLogManager;
    }
    protected void onHomingAgent(TransactionLog transactionLog){
        if(!serviceContext.node().homingAgentEnabled()) return;
        serviceContext.schedule(new ScheduleRunner(100,()->{
            try {
                String[] headers = new String[]{
                        Session.TARANTULA_ACCESS_KEY,"accesskey"
                };
                logger.warn(serviceContext.httpClientProvider().post(serviceContext.node().homingAgentHost(), "log", headers, transactionLog.toBinary()));
            }catch (Exception ex){
                logger.error("err",ex);
            }
        }));
    }

}
