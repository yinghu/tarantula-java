package com.tarantula.admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Statistics;
import com.icodesoftware.service.Metrics;
import com.icodesoftware.util.ResponseHeader;

public class MetricsContext extends ResponseHeader {

    public Metrics metrics;
    public MetricsContext(){
        this.successful = true;
    }
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("successful",successful);
        jsonObject.addProperty("id",metrics.statistics().key().asString());
        JsonArray ja = new JsonArray();
        for(Statistics.Entry kv : metrics.statistics().summary()){
            JsonObject xv = new JsonObject();
            xv.addProperty("name", kv.name());
            xv.addProperty("daily",kv.daily());
            xv.addProperty("weekly",kv.weekly());
            xv.addProperty("monthly",kv.monthly());
            xv.addProperty("yearly",kv.yearly());
            xv.addProperty("total",kv.total());
            ja.add(xv);
        }
        jsonObject.add("stats",ja);
        return jsonObject;
    }


}
