package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.Connection;

import java.lang.reflect.Type;

/**
 * Updated by yinghu on 9/8/2019.
 */
public class ConnectionSerializer implements JsonSerializer<Connection>{
    public JsonElement serialize(Connection onConnection, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jp = (JsonObject) new ResponseSerializer().serialize(onConnection,type,jsonSerializationContext);
        jp.addProperty("type",onConnection.type());
        jp.addProperty("serverId",onConnection.serverId());
        jp.addProperty("secured",onConnection.secured());
        jp.addProperty("protocol",onConnection.protocol());
        jp.addProperty("host",onConnection.host());
        jp.addProperty("path",onConnection.path());
        jp.addProperty("port",onConnection.port());
        return jp;
    }
}
