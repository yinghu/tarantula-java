package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Property;
import com.icodesoftware.util.RecoverableObject;


public class MetricsSnapshotResponse extends RecoverableObject{


    public String memberId;
    public JsonArray metrics;


    public MetricsSnapshotResponse(String memberId){
        this.memberId = memberId;
        this.metrics = new JsonArray();
    }

    public void snapshot(Property[] snapshot){
        for(Property p : snapshot) {
            JsonObject m = new JsonObject();
            m.addProperty("x",p.name());
            m.addProperty("y",p.value().toString());
            metrics.add(m);
        }
    }
    public void archive(String name,Object value){
        JsonObject m = new JsonObject();
        m.addProperty("x",name);
        m.addProperty("y",value.toString());
        metrics.add(m);
    }

    @Override
    public synchronized JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("memberId",memberId);
        jsonObject.add("data",metrics);
        return jsonObject;
    }
}
