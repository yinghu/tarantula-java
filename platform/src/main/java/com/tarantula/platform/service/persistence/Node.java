package com.tarantula.platform.service.persistence;


import com.tarantula.platform.DistributionKey;
import com.tarantula.platform.NoReplicationObject;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.Map;

/**
 * Updated by yinghu lu on 6/16/2020
 */
public class Node extends NoReplicationObject {

    public String bucketName;
    public String nodeName;

    public Node(){
        this.vertex = "Node";
    }

    public Node(String bucketName,String nodeName){
        this();
        this.bucketName = bucketName;
        this.nodeName = nodeName;
    }

    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableRegistry.NODE_CID;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",bucketName);//lobby id
        this.properties.put("2",nodeName);//game cluster id
        //this.properties.put("3",bucketId);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.bucketName = (String)properties.get("1");
        this.nodeName = (String)properties.get("2");
        //this.bucketId = ((Number)properties.getOrDefault("3",0)).intValue();
    }

    @Override
    public Key key(){
        return new DistributionKey(bucketName,nodeName);
    }
    public String toString(){
        return "Bucket ["+bucketName+"] On Node ["+nodeName+"]";
    }
}
