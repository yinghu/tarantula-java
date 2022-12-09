package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Property;
import com.icodesoftware.util.RecoverableObject;


public class MetricsSnapshotRequest extends RecoverableObject{


    public String memberId;
    public String name;
    public String category;
    public String classifier;
    public JsonArray metrics;

    public MetricsSnapshotRequest(String name,String category,String classifier){
        this.name = name;
        this.category = category;
        this.classifier = classifier;
    }
    public MetricsSnapshotRequest(String memberId){
        this.memberId = memberId;
    }

    public void snapshot(Property[] snapshot){
        this.metrics = new JsonArray();
        for(Property p : snapshot) {
            JsonObject m = new JsonObject();
            m.addProperty("x",p.name());
            m.addProperty("y",p.value().toString());
            metrics.add(m);
        }
    }

    public synchronized void snapshot(JsonObject snapshot){
        this.metrics = new JsonArray();
        memberId = snapshot.get("memberId").getAsString();
        snapshot.get("metrics").getAsJsonArray().forEach(m->metrics.add(m));
    }

    @Override
    public synchronized JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        boolean suc = metrics !=null;
        jsonObject.addProperty("successful",suc);
        if(!suc) return jsonObject;
        jsonObject.addProperty("memberId",memberId);
        jsonObject.add("metrics",metrics);
        return jsonObject;
    }

    public String toString(){
        return name+"_"+category+"_"+classifier;
    }

}
