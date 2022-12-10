package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.util.SystemUtil;

import java.time.LocalDateTime;


public class MetricsSnapshotRequest extends RecoverableObject{

    public String name;
    public String category;
    public String classifier;
    public LocalDateTime endTime;
    private JsonArray metrics;
    private boolean loaded;
    public boolean archived;

    public MetricsSnapshotRequest(String name,String category,String classifier){
        this.name = name;
        this.category = category;
        this.classifier = classifier;
        this.archived = false;
    }
    public MetricsSnapshotRequest(String name,String category,String classifier,LocalDateTime endTime){
        this.name = name;
        this.category = category;
        this.classifier = classifier;
        this.endTime = endTime;
        this.archived = true;
    }

    public synchronized void reset(){
        metrics = new JsonArray();
        loaded = false;
    }
    public synchronized void snapshot(JsonObject snapshot){
        metrics.add(snapshot);
    }
    public synchronized void loaded(){
        loaded = true;
    }

    @Override
    public synchronized JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",loaded);
        if(!loaded) return jsonObject;
        jsonObject.add("metrics",metrics);
        return jsonObject;
    }

    public String toString(){
        return archived? SystemUtil.oid() : (name+"_"+category+"_"+classifier);
    }

}
