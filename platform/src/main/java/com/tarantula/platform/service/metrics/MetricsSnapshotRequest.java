package com.tarantula.platform.service.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Property;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;


public class MetricsSnapshotRequest extends RecoverableObject{



    public final String memberId;
    public final String name;
    public final String category;
    public final String classifier;
    public final JsonArray metrics;

    public MetricsSnapshotRequest(String memberId,String name,String category,String classifier){
        this.memberId = memberId;
        this.name = name;
        this.category = category;
        this.classifier = classifier;
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
    public void snapshot(JsonArray snapshot){
        snapshot.forEach(m-> metrics.add(m));
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("memberId",memberId);
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("category",category);
        jsonObject.addProperty("classifier",classifier);
        jsonObject.add("metrics",metrics);
        return jsonObject;
    }

    public static MetricsSnapshotRequest parse(String payload){
        JsonObject jsonObject = JsonUtil.parse(payload);
        String m = jsonObject.get("memberId").getAsString();
        String n = jsonObject.get("name").getAsString();
        String c = jsonObject.get("category").getAsString();
        String f = jsonObject.get("classifier").getAsString();
        MetricsSnapshotRequest request = new MetricsSnapshotRequest(m,n,c,f);
        request.snapshot(jsonObject.get("metrics").getAsJsonArray());
        return request;
    }
}
