package com.tarantula.platform.service.persistence.berkeley;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.*;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.persistence.MapStoreListener;


public class ScopedReplicationProxy implements MapStoreListener,ServiceProvider{

    protected ServiceContext serviceContext;

    protected ClusterProvider.Node localNode;

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
