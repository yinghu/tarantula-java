package com.tarantula.platform.service.persistence;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.Distributable;
import com.icodesoftware.Event;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.*;
import com.tarantula.platform.event.IntegrationReplicationEvent;


public class ScopedReplicationProxy implements MapStoreListener,ServiceProvider{

    private final static String CONFIG = "replication-service-settings";
    protected ServiceContext serviceContext;

    protected ClusterProvider.Node localNode;

    private final int scope;
    protected boolean asyncDistributing;
    protected long syncInterval;

    protected int maxPendingSize;
    protected int maxBatchSize;
    public ScopedReplicationProxy(int scope){
        this.scope = scope;
    }

    @Override
    public <T extends Recoverable> void onBackingUp(Metadata metadata, String key, T t) {

    }

    @Override
    public void onDistributing(Metadata metadata, String stringKey, byte[] key, byte[] value) {

    }


    @Override
    public byte[] onRecovering(Metadata metadata, String stringKey, byte[] key) {
        return null;
    }

    @Override
    public void onDeleting(Metadata metadata, byte[] key) {

    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        Configuration configuration = serviceContext.configuration(CONFIG);
        JsonObject conf = null;
        if(scope== Distributable.INTEGRATION_SCOPE){
            conf = ((JsonElement)configuration.property("integration")).getAsJsonObject();
        }
        else if(scope==Distributable.DATA_SCOPE){
            conf = ((JsonElement)configuration.property("data")).getAsJsonObject();
        }
        if(conf==null) return;
        asyncDistributing = conf.get("asyncDistributing").getAsBoolean();
        if(asyncDistributing){
            maxPendingSize = conf.get("maxPendingSize").getAsInt();
            syncInterval = conf.get("syncIntervalSeconds").getAsInt()*1000;
            maxBatchSize = conf.get("maxBatchSize").getAsInt();
        }
        setup();
    }


    public int maxReplicationNumber(){
        return serviceContext.clusterProvider().maxReplicationNumber();
    }
    protected ClusterProvider.Node nextNode(){
        return serviceContext.keyIndexService().nextNode();
    }

    protected ClusterProvider.Node[] nextNodeList(int expected){
        return serviceContext.keyIndexService().nextNodeList(expected);
    }

    protected ClusterProvider.Node[] nodeList(KeyIndex keyIndex){
        return serviceContext.keyIndexService().nodeList(keyIndex);
    }

    protected ClusterProvider.Node[] nodeList(KeyIndex keyIndex,int expected){
        return serviceContext.keyIndexService().nodeList(keyIndex,expected);
    }

    protected KeyIndex lookup(String source,String key){
        return this.serviceContext.keyIndexService().lookup(source,key);
    }

    protected void setup(){}
    protected void replicate(ClusterProvider.Node target){

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void waitForData() {
        this.localNode = serviceContext.node();
    }
}
