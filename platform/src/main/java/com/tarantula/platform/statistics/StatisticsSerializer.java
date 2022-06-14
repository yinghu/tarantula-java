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
            xv.addProperty("Name",kv.name());
            xv.addProperty("Daily",kv.daily());
            xv.addProperty("Weekly",kv.weekly());
            xv.addProperty("Monthly",kv.monthly());
            xv.addProperty("Yearly",kv.yearly());
            xv.addProperty("Total",kv.total());
            ja.add(xv);
        }
        jo.addProperty("successful",true);
        jo.add("_categories",ja);
        return jo;
    }
}
