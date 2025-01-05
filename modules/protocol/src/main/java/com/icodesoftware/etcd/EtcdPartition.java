package com.icodesoftware.etcd;

public class EtcdPartition {
    public final int partition;
    private EtcdNode etcdNode;
    public EtcdPartition(int partition){
        this.partition = partition;
    }

    public EtcdNode onPartition(){
        return etcdNode;
    }
    public void onPartition(EtcdNode etcdNode){
        this.etcdNode = etcdNode;
    }
}
