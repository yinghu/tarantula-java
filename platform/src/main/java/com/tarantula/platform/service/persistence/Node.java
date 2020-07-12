package com.tarantula.platform.service.persistence;

/**
 * Updated by yinghu lu on 6/16/2020
 */
public class Node{

    public String bucketName;
    public String nodeName;

    public String bucketId;
    public String nodeId;

    public Node(String bucketName,String nodeName){
        this.bucketName = bucketName;
        this.nodeName = nodeName;
    }

    public String toString(){
        return "Bucket ["+bucketName+"] On Node ["+nodeName+"]";
    }
}
