package com.tarantula.platform.service.persistence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.*;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;


public class ScopedReplicationProxy implements MapStoreListener,ServiceProvider{

    protected ServiceContext serviceContext;

    protected ClusterProvider.Node localNode;

    protected ArrayBlockingQueue<OffHeapOnReplication> pendingReplication;
    public ScopedReplicationProxy(){
        pendingReplication = new ArrayBlockingQueue<>(10);
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
        ArrayList<OffHeapOnReplication> dropList = new ArrayList<>();
        this.pendingReplication.drainTo(dropList);
        dropList.forEach(offHeapOnReplication -> offHeapOnReplication.drop());
    }

    @Override
    public void waitForData() {
        this.localNode = serviceContext.node();
    }
}
