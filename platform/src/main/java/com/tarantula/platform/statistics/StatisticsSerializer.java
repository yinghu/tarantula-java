package com.tarantula.platform.statistics;

import com.google.gson.*;
import com.icodesoftware.Statistics;

import java.lang.reflect.Type;

public class StatisticsSerializer implements JsonSerializer<Statistics> {

    public JsonElement serialize(Statistics statistics, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo  = new JsonObject();
        JsonArray ja = new JsonArray();
        for(Statistics.Entry kv : statistics.summary()){
            JsonObject xv = new JsonObject();
            xv.addProperty("name",kv.name());
            xv.addProperty("daily",kv.daily());
            xv.addProperty("weekly",kv.weekly());
            xv.addProperty("monthly",kv.monthly());
            xv.addProperty("yearly",kv.yearly());
            xv.addProperty("total",kv.total());
            ja.add(xv);
        }
        jo.addProperty("successful",true);
        jo.add("stats",ja);
        return jo;
    }
}
