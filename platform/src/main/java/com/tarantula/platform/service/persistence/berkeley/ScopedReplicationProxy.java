package com.tarantula.platform.service.persistence.berkeley;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.*;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.persistence.DataStoreOnPartition;
import com.tarantula.platform.service.persistence.MapStoreListener;
import com.tarantula.platform.service.persistence.RevisionObject;

import java.util.concurrent.ConcurrentHashMap;

public class ScopedReplicationProxy implements MapStoreListener,ServiceProvider, ClusterProvider.NodeListener {

    protected ServiceContext serviceContext;
    protected  DataStoreOnPartition[] dataStoreOnPartitions;
    protected ConcurrentHashMap<String, ClusterProvider.Node> nodeList = new ConcurrentHashMap<>();

    protected DataStoreProvider dataStoreProvider;
    public ScopedReplicationProxy(DataStoreProvider dataStoreProvider){
        this.dataStoreProvider = dataStoreProvider;
    }

    @Override
    public <T extends Recoverable> void onBackingUp(Metadata metadata, String key, T t) {

    }

    @Override
    public void onDistributing(Metadata metadata, String stringKey, byte[] key, byte[] value) {

    }

    @Override
    public void onDistributing(Metadata metadata, String stringKey, byte[] key, RevisionObject value) {
        //logger.warn("distributing ["+stringKey+"]");
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
        serviceContext.clusterProvider().registerNodeListener(this);
    }





    @Override
    public void nodeAdded(ClusterProvider.Node node) {
        nodeList.put(node.nodeName(),node);
    }

    @Override
    public void nodeRemoved(ClusterProvider.Node node) {
        nodeList.remove(node.nodeName());
    }

    public DataStoreOnPartition onPartition(byte[] key){
        int partition = this.serviceContext.clusterProvider().partition(key);
        return this.dataStoreOnPartitions[partition];
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
