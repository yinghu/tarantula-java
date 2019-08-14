package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.Statistics;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Updated by yinghu on 10/7/2018.
 */
public class StatisticsSerializer implements JsonSerializer<Statistics> {

    public JsonElement serialize(Statistics statistics, Type type, JsonSerializationContext jsonSerializationContext) {

        JsonObject jo  = new JsonObject();
        jo.addProperty("applicationId",statistics.applicationId());
        jo.addProperty("instanceId",statistics.instanceId());
        jo.addProperty("header",statistics.leaderBoardHeader());
        jo.addProperty("name",statistics.name());
        JsonArray ja = new JsonArray();
        for(Map.Entry<String,Double> kv : statistics.list().entrySet()){
            JsonObject xv = new JsonObject();
            xv.addProperty("name",kv.getKey());
            xv.addProperty("value",kv.getValue());
            ja.add(xv);
        }
        jo.add("summary",ja);
        return jo;
    }
}
