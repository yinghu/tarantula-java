package com.tarantula.platform.service.cluster;

import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.service.OnPartition;
import com.tarantula.platform.IndexKey;

import java.util.Map;


public class PartitionState extends RecoverableObject implements OnPartition {
    public int partition;
    public boolean opening;
    public int version;

    public PartitionState(){

    }
    public PartitionState(int partition){
        this.partition = partition;
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
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",opening);
        this.properties.put("2",version);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.opening = (Boolean)properties.get("1");
        this.version = ((Number)properties.get("2")).intValue();
    }


    public int getClassId() {
        return PortableRegistry.PARTITION_STATE_OID;
    }
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    public Key key(){
        return new IndexKey(this.bucket,"partition",partition);
    }
}
