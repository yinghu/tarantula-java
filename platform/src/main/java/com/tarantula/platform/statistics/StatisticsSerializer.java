package com.tarantula.platform.statistics;

import com.google.gson.*;
import com.icodesoftware.Statistics;

import java.lang.reflect.Type;

public class StatisticsSerializer implements JsonSerializer<Statistics> {
    @Override
    public JsonElement serialize(Statistics statistics, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo  = new JsonObject();
        JsonArray ja = new JsonArray();
        for(Statistics.Entry entry : statistics.summary()){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name",entry.name());
            jsonObject.addProperty("daily",entry.daily());
            jsonObject.addProperty("weekly",entry.weekly());
            jsonObject.addProperty("monthly",entry.monthly());
            jsonObject.addProperty("yearly",entry.yearly());
            jsonObject.addProperty("total",entry.total());
            ja.add(jsonObject);
        }
        jo.addProperty("successful",true);
        jo.add("entries",ja);
        return jo;

    }
}
