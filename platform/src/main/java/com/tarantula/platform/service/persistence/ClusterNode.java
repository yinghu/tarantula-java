package com.tarantula.platform.service.persistence;


import com.google.gson.JsonObject;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import java.time.format.DateTimeFormatter;
import java.util.Map;


public class ClusterNode extends RecoverableObject implements ClusterProvider.Node {

    public String bucketName;
    public String nodeName;

    public String bucketId;
    public String nodeId;
    public String memberId;
    public String address;
    public long startTime;

    public String deploymentId;
    public String clusterNameSuffix;
    public int partitionNumber;
    //public int clusterPartitionNumber;

    public String deployDirectory;
    public String servicePushAddress;

    public boolean runAsMirror;
    public boolean backupEnabled;
    public boolean dailyBackupEnabled;
    public String dataStoreDirectory;

    public ClusterNode(String bucketName, String nodeName,int partitionNumber){
        this.bucketName = bucketName;
        this.nodeName = nodeName;
        this.partitionNumber = partitionNumber;
        //this.clusterPartitionNumber = clusterPartitionNumber;
    }
    public ClusterNode(){
    }

    public ClusterNode(String memberId){
        this.memberId = memberId;
    }

    public String toString(){
        return "Bucket ["+bucketName+"] On Node ["+nodeName+"]";
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
    public String bucketId() {
        return bucketId;
    }

    @Override
    public String nodeId() {
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

    public String deploymentId(){
        return this.deploymentId;
    }

    public int partitionNumber(){
        return partitionNumber;
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

    public boolean runAsMirror(){return runAsMirror;}
    public boolean backupEnabled(){return backupEnabled;}
    public boolean dailyBackupEnabled(){ return this.dailyBackupEnabled;}
    public String dataStoreDirectory(){
        return this.dataStoreDirectory;
    }
    @Override
    public JsonObject toJson(){
        return _toJson(true);
    }

    @Override
    public Map<String,Object> toMap(){
        properties.put("bucketName",this.bucketName);
        properties.put("bucketId",this.bucketId);
        properties.put("nodeName",this.nodeName);
        properties.put("nodeId",this.nodeId);
        properties.put("memberId",this.memberId);
        properties.put("address",this.address);
        properties.put("startTime",this.startTime);
        properties.put("clusterNameSuffix",this.clusterNameSuffix);
        properties.put("partitionNumber",this.partitionNumber);
        properties.put("deploymentId",this.deploymentId);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.bucketName = (String) properties.get("bucketName");
        this.bucketId = (String) properties.get("bucketId");
        this.nodeName = (String) properties.get("nodeName");
        this.nodeId = (String)properties.get("nodeId");
        this.memberId = (String)properties.get("memberId");
        this.address = (String)properties.get("address");
        this.startTime = ((Number)properties.getOrDefault("startTime",0)).longValue();
        this.clusterNameSuffix = (String)properties.get("clusterNameSuffix");
        this.partitionNumber = ((Number)properties.getOrDefault("partitionNumber",0)).intValue();
        this.deploymentId = (String)properties.get("deploymentId");
    }

    /**
    public byte[] toBinary(){
        return _toJson(false).toString().getBytes();
    }
    public void fromBinary(byte[] payload){
        JsonObject jsonObject = JsonUtil.parse(payload);
        this.bucketName = jsonObject.get("bucketName").getAsString();
        this.bucketId = jsonObject.get("bucketId").getAsString();
        this.nodeName = jsonObject.get("nodeName").getAsString();
        this.nodeId = jsonObject.get("nodeId").getAsString();
        this.memberId = jsonObject.get("memberId").getAsString();
        this.address = jsonObject.get("address").getAsString();
        this.startTime = jsonObject.get("startTime").getAsLong();
        this.clusterNameSuffix = jsonObject.get("clusterNameSuffix").getAsString();
        this.partitionNumber = jsonObject.get("partitionNumber").getAsInt();
        this.deploymentId = jsonObject.get("deploymentId").getAsString();
    }**/
    private JsonObject _toJson(boolean toWeb){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("bucketName",bucketName);
        jsonObject.addProperty("bucketId",bucketId);
        jsonObject.addProperty("nodeName",nodeName);
        jsonObject.addProperty("nodeId",nodeId);
        jsonObject.addProperty("memberId",memberId);
        jsonObject.addProperty("address",address);
        jsonObject.addProperty("clusterNameSuffix",clusterNameSuffix);
        jsonObject.addProperty("partitionNumber",partitionNumber);
        jsonObject.addProperty("deploymentId",deploymentId);
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
        return this.memberId.hashCode();
    }
    @Override
    public boolean equals(Object obj){
        ClusterNode r = (ClusterNode)obj;
        return this.memberId.equals(r.memberId());
    }

}
