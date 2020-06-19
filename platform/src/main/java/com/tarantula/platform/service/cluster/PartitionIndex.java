package com.tarantula.platform.service.cluster;

import com.tarantula.DataStore;
import com.tarantula.Recoverable;
import com.tarantula.platform.DistributionKey;
import com.tarantula.platform.RecoverableObject;

import java.util.Map;

public class PartitionIndex extends RecoverableObject implements DataStore.Updatable {

    public PartitionIndex(){}
    public PartitionIndex(String bucket,String index){
        this.bucket = bucket;
        this.index = index;
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",sequence);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.sequence = ((Number)properties.getOrDefault("1",1000)).longValue();
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
