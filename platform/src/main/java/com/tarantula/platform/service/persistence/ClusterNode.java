package com.tarantula.platform.service.persistence;


import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.icodesoftware.service.ClusterProvider;

import java.io.IOException;
import java.time.LocalDateTime;

public class ClusterNode implements ClusterProvider.Node {

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
    public LocalDateTime startTime() {
        return null;
    }


    public int getFactoryId() {
        return 0;
    }

    public int getClassId() {
        return 0;
    }
}
