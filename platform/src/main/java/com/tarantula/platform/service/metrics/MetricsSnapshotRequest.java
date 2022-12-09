package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;


public class MetricsSnapshotRequest extends RecoverableObject{


    //public String memberId;
    public String name;
    public String category;
    public String classifier;
    public JsonArray metrics;
    private boolean loaded;

    public MetricsSnapshotRequest(String name,String category,String classifier){
        this.name = name;
        this.category = category;
        this.classifier = classifier;
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
        return name+"_"+category+"_"+classifier;
    }

}
