package com.tarantula.platform.service;

import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ClusterSummary implements ClusterProvider.Summary {

    private final String clusterName;
    private final long startTime;

    private final ConcurrentHashMap<String, ClusterProvider.Node> nodeList;
    public ClusterSummary(String clusterName){
        this.clusterName = clusterName;
        this.startTime = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        this.nodeList = new ConcurrentHashMap<>();
    }
    public String clusterName(){
        return clusterName;
    }
    public long startTime(){
        return startTime;
    }

    public List<ClusterProvider.Node> clusterNodes(){
        ArrayList<ClusterProvider.Node> _nodes = new ArrayList<>();
        nodeList.forEach((k,n)-> _nodes.add(n));
        return _nodes;
    }

    //operations
    public void register(ClusterProvider.Node node){
        nodeList.put(node.nodeId(),node);
    }


}
