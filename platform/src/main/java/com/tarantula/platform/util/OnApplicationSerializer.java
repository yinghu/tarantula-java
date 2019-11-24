package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.platform.OnApplicationHeader;

import java.lang.reflect.Type;

/**
 * Updated by yinghu on 8/14/2019.
 */
public class OnApplicationSerializer implements JsonSerializer<OnApplicationHeader> {
    public JsonElement serialize(OnApplicationHeader onApplication, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject)new ResponseSerializer().serialize(onApplication,type,jsonSerializationContext);
        jo.addProperty("systemId",onApplication.systemId());
        jo.addProperty("stub",onApplication.stub());
        jo.addProperty("name",onApplication.name());
        jo.addProperty("applicationId",onApplication.applicationId());
        jo.addProperty("instanceId",onApplication.instanceId());
        jo.addProperty("balance",onApplication.accessMode());
        jo.addProperty("tournamentEnabled",onApplication.tournamentEnabled());
        jo.addProperty("ticket",onApplication.ticket());
        jo.addProperty("index",onApplication.index());
        if(onApplication.connection()!=null){
            jo.add("connection",new ConnectionSerializer().serialize(onApplication.connection(),type,jsonSerializationContext));
        }
        return jo;
    }
}
