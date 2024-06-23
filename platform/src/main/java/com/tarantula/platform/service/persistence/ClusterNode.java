package com.tarantula.platform.service.persistence;


import com.google.gson.JsonObject;
import com.icodesoftware.lmdb.BufferProxy;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import java.time.format.DateTimeFormatter;


public class ClusterNode extends RecoverableObject implements ClusterProvider.Node {

    public String bucketName;
    public String nodeName;

    public long bucketId;
    public long nodeId;
    public String memberId;
    public String address;
    public long startTime;

    public long deploymentId;
    public String clusterNameSuffix;
    public int partitionNumber;
    public int bucketNumber;
    public String deployDirectory;
    public String servicePushAddress;

    public boolean dailyBackupEnabled;
    public String dataStoreDirectory;
    public boolean homingAgentEnabled;
    public String homingAgentHost;
    public ClusterNode(String bucketName, String nodeName,int partitionNumber,int bucketNumber){
        this.bucketName = bucketName;
        this.nodeName = nodeName;
        this.partitionNumber = partitionNumber;
        this.bucketNumber = bucketNumber;
    }
    public ClusterNode(){
    }

    public String toString(){
        return "Bucket Name ["+bucketName+"] On Node ["+nodeName+"] partitions ["+partitionNumber+"] buckets ["+bucketNumber+"]";
    }

    @Override
    public String bucketName() {
        return bucketName;
    }

    @Override
    public String nodeName() {
        return nodeName;
    }

    @Override
    public long bucketId() {
        return bucketId;
    }

    @Override
    public long nodeId() {
        return nodeId;
    }

    @Override
    public String memberId() {
        return memberId;
    }

    @Override
    public String address(){
        return address;
    }

    @Override
    public long startTime() {
        return startTime;
    }

    public long deploymentId(){
        return this.deploymentId;
    }

    public int partitionNumber(){
        return partitionNumber;
    }
    public int bucketNumber(){
        return bucketNumber;
    }
    public String clusterNameSuffix(){
        return this.clusterNameSuffix;
    }


    public String deployDirectory(){
        return deployDirectory;
    }
    public String servicePushAddress(){
        return servicePushAddress;
    }

    public boolean dailyBackupEnabled(){ return this.dailyBackupEnabled;}
    public String dataStoreDirectory(){
        return this.dataStoreDirectory;
    }
    public boolean homingAgentEnabled(){
        return homingAgentEnabled;
    }

    @Override
    public String homingAgentHost() {
        return homingAgentHost;
    }

    @Override
    public JsonObject toJson(){
        return _toJson(true);
    }


    @Override
    public byte[] toBinary() {
        DataBuffer buffer = BufferProxy.buffer(500,false);
        buffer.writeUTF8(bucketName);
        buffer.writeLong(bucketId);
        buffer.writeUTF8(nodeName);
        buffer.writeLong(nodeId);
        buffer.writeUTF8(memberId);
        buffer.writeUTF8(address);
        buffer.writeLong(startTime);
        buffer.writeUTF8(clusterNameSuffix);
        buffer.writeLong(deploymentId);
        buffer.writeInt(partitionNumber);
        buffer.writeInt(bucketNumber);
        return buffer.array();
    }

    @Override
    public void fromBinary(byte[] data){
        DataBuffer buffer = BufferProxy.wrap(data);
        this.bucketName = buffer.readUTF8();
        this.bucketId = buffer.readLong();
        this.nodeName = buffer.readUTF8();
        this.nodeId = buffer.readLong();
        this.memberId =buffer.readUTF8();
        this.address = buffer.readUTF8();
        this.startTime =buffer.readLong();
        this.clusterNameSuffix =buffer.readUTF8();
        this.deploymentId = buffer.readLong();
        this.partitionNumber = buffer.readInt();
        this.bucketNumber = buffer.readInt();
    }

    private JsonObject _toJson(boolean toWeb){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("bucketName",bucketName);
        jsonObject.addProperty("bucketId","A_"+bucketId);
        jsonObject.addProperty("nodeName",nodeName);
        jsonObject.addProperty("nodeId","A_"+nodeId);
        jsonObject.addProperty("memberId",memberId);
        jsonObject.addProperty("address",address);
        jsonObject.addProperty("clusterNameSuffix",clusterNameSuffix);
        jsonObject.addProperty("partitionNumber",partitionNumber);
        jsonObject.addProperty("bucketNumber",bucketNumber);
        jsonObject.addProperty("deploymentId","A_"+deploymentId);
        if(toWeb){
            jsonObject.addProperty("startTime",TimeUtil.fromUTCMilliseconds(startTime).format(DateTimeFormatter.ISO_DATE_TIME));
        }
        else{
            jsonObject.addProperty("startTime",startTime);
        }
        return jsonObject;
    }

    @Override
    public int hashCode(){
        if(memberId!=null) return memberId.hashCode();
        return this.nodeName.hashCode();
    }
    @Override
    public boolean equals(Object obj){
        ClusterNode r = (ClusterNode)obj;
        if(memberId!=null) return memberId.equals(r.memberId);
        return this.nodeName.equals(r.nodeName());
    }

}
