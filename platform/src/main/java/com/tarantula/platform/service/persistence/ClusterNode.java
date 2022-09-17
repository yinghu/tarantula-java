package com.tarantula.platform.service.persistence;


import com.google.gson.JsonObject;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;


public class ClusterNode extends RecoverableObject implements ClusterProvider.Node {

    public String bucketName;
    public String nodeName;

    public String bucketId;
    public String nodeId;
    public String memberId;
    public long startTime;

    public ClusterNode(String bucketName, String nodeName){
        this.bucketName = bucketName;
        this.nodeName = nodeName;
    }

    public ClusterNode(){
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
    public long startTime() {
        return startTime;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("bucketName",bucketName);
        jsonObject.addProperty("bucketId",bucketId);
        jsonObject.addProperty("nodeName",nodeName);
        jsonObject.addProperty("nodeId",nodeId);
        jsonObject.addProperty("memberId",memberId);
        return jsonObject;
    }

    public byte[] toBinary(){
        return toJson().toString().getBytes();
    }
    public void fromBinary(byte[] payload){
        JsonObject jsonObject = JsonUtil.parse(payload);
        this.bucketName = jsonObject.get("bucketName").getAsString();
        this.bucketId = jsonObject.get("bucketId").getAsString();
        this.nodeName = jsonObject.get("nodeName").getAsString();
        this.nodeId = jsonObject.get("nodeId").getAsString();
        this.memberId = jsonObject.get("memberId").getAsString();
        this.startTime = jsonObject.get("startTime").getAsLong();

    }

}
