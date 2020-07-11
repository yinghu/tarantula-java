package com.tarantula.platform.service.cluster;

import com.tarantula.Countable;
import com.tarantula.DataStore;
import com.tarantula.Recoverable;
import com.tarantula.platform.*;

import java.util.Map;

public class PartitionIndex extends NoReplicationObject implements DataStore.Updatable, Countable {

    public PartitionIndex(){
        //this.vertex="PartitionIndex";
    }
    public PartitionIndex(String bucket,String label,String index,int initialSeed){
        this();
        this.bucket = bucket;
        this.label = label;
        this.index = index;
        this.version = initialSeed;
    }
    public synchronized int count(int delta){
        return (version=version+delta);
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",version);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.version = ((Number)properties.getOrDefault("1",1000)).intValue();
    }

    public int getClassId() {
        return PortableRegistry.PARTITION_INDEX_OID;
    }
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    public String distributionKey() {
        if(this.bucket!=null&&this.label!=null){
            return new StringBuffer(this.bucket).append(Recoverable.PATH_SEPARATOR).append(label).toString();
        }
        else{
            return null;
        }
    }
    @Override
    public void distributionKey(String distributionKey){
        try{
            String[] klist = distributionKey.split(Recoverable.PATH_SEPARATOR);
            this.bucket = klist[0];
            this.label = klist[2];
        }catch (Exception ex){
            //ignore wrong format key
        }
    }
    //@Override
    //public Key key(){//format bucket/partition
        //return new AssociateKey(this.bucket,label);
    //}
}
