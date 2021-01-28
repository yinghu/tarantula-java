package com.tarantula.platform.service.cluster;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.util.DistributionKey;
import com.tarantula.platform.*;

import java.util.Map;

public class PartitionIndex extends NoReplicationObject implements DataStore.Updatable {

    private int start = 0;
    private int end = 0;
    public PartitionIndex(){

    }
    public PartitionIndex(String bucket,String label,int initialSeed){
        this.bucket = bucket;
        this.label = label;
        this.version = initialSeed;
    }
    public synchronized int sequence(){
        if(end-start>0){
            int ct = start;
            start++;
            return ct;
        }
        start = version;
        end = start+10;
        version = end;
        this.update();
        int ct = start;
        start++;
        return ct;
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
    @Override
    public byte[] toBinary() {
        DataBuffer dataBuffer = new DataBuffer(4);
        dataBuffer.putInt(version);
        return dataBuffer.toArray();
    }
    @Override
    public void fromBinary(byte[] payload){
        DataBuffer dataBuffer = new DataBuffer(payload);
        version = dataBuffer.getInt();
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
            this.label = klist[1];
        }catch (Exception ex){
            //ignore wrong format key
        }
    }
    @Override
    public Key key(){//format bucket/label
        return new DistributionKey(this.bucket,label);
    }
}
