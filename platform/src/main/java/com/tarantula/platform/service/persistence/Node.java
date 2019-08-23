package com.tarantula.platform.service.persistence;


import com.tarantula.platform.DistributionKey;
import com.tarantula.platform.NoReplicationObject;
import com.tarantula.platform.service.cluster.PortableRegistry;

import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 4/18/2018.
 */
public class Node extends NoReplicationObject {

    public String bucketName;
    public String nodeName;
    public int bucketId;

    public Node(){
        this.vertex = "Node";
        this.binary = true;
    }

    public Node(String bucketName,String nodeName,int bucketId){
        this();
        this.bucketName = bucketName;
        this.nodeName = nodeName;
        this.bucketId = bucketId;
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
    public byte[] toByteArray(){
        byte[] bn = bucketName.getBytes();
        byte[] nn = nodeName.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(12+bn.length+nn.length);
        buffer.putInt(bucketId);
        buffer.putInt(bn.length);
        buffer.put(bn);
        buffer.putInt(nn.length);
        buffer.put(nn);
        return buffer.array();
    }
    @Override
    public void fromByteArray(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.bucketId = buffer.getInt();
        int len = buffer.getInt();
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<len;i++){
            sb.append((char) buffer.get());
        }
        bucketName = sb.toString();
        len = buffer.getInt();
        sb.setLength(0);
        for(int i=0;i<len;i++){
            sb.append((char) buffer.get());
        }
        nodeName = sb.toString();
    }
    @Override
    public Key key(){
        return new DistributionKey(bucketName,bucketId+"");
    }
    public String toString(){
        return "Bucket ["+bucketName+"/"+bucketId+"] On Node ["+nodeName+"]";
    }
}
