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

    public ClusterNode(String bucketName, String nodeName){
        this.bucketName = bucketName;
        this.nodeName = nodeName;
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
        return null;
    }

    @Override
    public long startTime() {
        return 0;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();

        return jsonObject;
    }

    public byte[] toBinary(){
        return toJson().toString().getBytes();
    }
    public void fromBinary(byte[] payload){
        JsonObject jsonObject = JsonUtil.parse(payload);

    }

}
