package com.icodesoftware.etcd;

import com.icodesoftware.service.OnPartition;

public class EtcdPartition implements OnPartition {
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

    @Override
    public int partition() {
        return partition;
    }

    @Override
    public boolean opening() {
        return true;
    }
}
