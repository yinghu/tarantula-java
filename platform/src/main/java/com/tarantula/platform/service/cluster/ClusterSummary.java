package com.tarantula.platform.service.cluster;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ClusterSummary extends RecoverableObject implements ClusterProvider.Summary {

    private final String clusterName;
    public int partitionNumber;

    private final ConcurrentHashMap<String, ClusterProvider.Node> nodeList;

    public ClusterSummary(String clusterName,int partitionNumber){
        this.clusterName = clusterName;
        this.partitionNumber = partitionNumber;
        this.nodeList = new ConcurrentHashMap<>();
    }
    public String clusterName(){
        return clusterName;
    }
    public int partitionNumber(){
        return partitionNumber;
    }

    public List<ClusterProvider.Node> clusterNodes(){
        ArrayList<ClusterProvider.Node> _nodes = new ArrayList<>();
        nodeList.forEach((k,n)-> _nodes.add(n));
        return _nodes;
    }

    //operations
    public void register(ClusterProvider.Node node){
        nodeList.put(node.nodeName(),node);
        //nodeList.put(node.memberId(),node);
    }
    public void unregister(ClusterProvider.Node node){
        ClusterProvider.Node removed = nodeList.remove(node.nodeName());
        //if(removed==null) return;
        //nodeList.remove(removed.memberId());
    }

    public ClusterProvider.Node node(String nodeName){
        return nodeList.get(nodeName);
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("clusterName",clusterName);
        jsonObject.addProperty("partitionNumber",partitionNumber);
        JsonArray nodes = new JsonArray();
        nodeList.forEach((k,n)->{
            nodes.add(n.toJson());
        });
        jsonObject.add("nodeList",nodes);
        return jsonObject;
    }


}
