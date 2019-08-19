package com.tarantula.demo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Updated 7/25/19 by yinghu
 */
public class TimerSerializer implements JsonSerializer<Timer> {
    @Override
    public JsonElement serialize(Timer timer, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("label",timer.label());
        jo.addProperty("hh",timer.hour);
        jo.addProperty("mm",timer.minute);
        jo.addProperty("ss",timer.second);
        jo.addProperty("ms",timer.millisecond);
        return jo;
    }
}
