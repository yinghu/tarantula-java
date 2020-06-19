package com.tarantula.platform.service.cluster;

import com.tarantula.Countable;
import com.tarantula.DataStore;
import com.tarantula.Recoverable;
import com.tarantula.platform.DistributionKey;
import com.tarantula.platform.RecoverableObject;

import java.util.Map;

public class PartitionIndex extends RecoverableObject implements DataStore.Updatable, Countable {

    public PartitionIndex(){}
    public PartitionIndex(String bucket,String index,int initialSeed){
        this.bucket = bucket;
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
        if(this.bucket!=null&&this.index!=null){
            return new StringBuffer(this.bucket).append(Recoverable.PATH_SEPARATOR).append(index).toString();
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
            this.index = klist[1];
        }catch (Exception ex){
            //ignore wrong format key
        }
    }
    @Override
    public Key key(){//format bucket/partition
        return new DistributionKey(this.bucket,index);
    }
}
