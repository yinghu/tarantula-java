package com.tarantula.platform.service.cluster;

import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.service.OnPartition;


public class PartitionState extends RecoverableObject implements OnPartition {
    public int partition;
    public boolean opening;
    public int version;

    public PartitionState(){

    }
    public PartitionState(int partition,boolean opening){
        this.partition = partition;
        this.opening = opening;
    }

    @Override
    public int partition() {
        return this.partition;
    }

    @Override
    public boolean opening() {
        return this.opening;
    }



    public int getClassId() {
        return PortableRegistry.PARTITION_STATE_OID;
    }
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

}
