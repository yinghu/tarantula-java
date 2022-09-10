package com.tarantula.platform.service;

import com.icodesoftware.service.ClusterProvider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClusterSummary implements ClusterProvider.Summary {

    public String clusterName(){
        return "";
    }
    public LocalDateTime startTime(){
        return LocalDateTime.now();
    }

    public List<ClusterProvider.Node> clusterNodes(){
        return new ArrayList<>();
    }
}
