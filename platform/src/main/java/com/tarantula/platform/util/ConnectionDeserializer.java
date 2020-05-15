package com.tarantula.platform.util;

import com.google.gson.*;

import com.tarantula.Connection;
import com.tarantula.platform.WebSocketConnection;

import java.lang.reflect.Type;

public class ConnectionDeserializer implements JsonDeserializer<Connection> {

    @Override
    public Connection deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        Connection desc = new WebSocketConnection();
        if(jo.has("type")){
            desc.type(jo.get("type").getAsString());
        }
        if(jo.has("serverId")){
            desc.serverId(jo.get("serverId").getAsString());
        }
        if(jo.has("secured")){
            desc.secured(jo.get("secured").getAsBoolean());
        }
        if(jo.has("protocol")){
            desc.protocol(jo.get("protocol").getAsString());
        }
        if(jo.has("path")){
            desc.path(jo.get("path").getAsString());
        }
        if(jo.has("host")){
            desc.host(jo.get("host").getAsString());
        }
        if(jo.has("port")){
            desc.port(jo.get("port").getAsInt());
        }
        if(jo.has("maxConnections")){
            desc.maxConnections(jo.get("maxConnections").getAsInt());
        }
        return desc;
    }
}
