package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.Response;

import java.lang.reflect.Type;

/**
 * Updated by yinghu on 9/6/2019
 */
public class ResponseSerializer implements JsonSerializer<Response> {

    public JsonElement serialize(Response response, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("command",response.command());
        jo.addProperty("label",response.label());
        jo.addProperty("message",response.message());
        jo.addProperty("successful",response.successful());
        return jo;
    }
}
