package com.tarantula.platform.util;

import com.google.gson.*;


import com.tarantula.platform.room.ConnectionStub;

import java.lang.reflect.Type;

public class ConnectionDeserializer implements JsonDeserializer<ConnectionStub> {

    @Override
    public ConnectionStub deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        return toConnection(jo);
    }
    private ConnectionStub toConnection(JsonObject jo){
        ConnectionStub desc = new ConnectionStub();
        if(jo.has("type")){
            desc.type(jo.get("type").getAsString());
        }
        if(jo.has("serverId")){
            desc.serverId(jo.get("serverId").getAsString());
        }
        if(jo.has("secured")){
            desc.secured(jo.get("secured").getAsBoolean());
        }
        if(jo.has("host")){
            desc.host(jo.get("host").getAsString());
        }
        if(jo.has("port")){
            desc.port(jo.get("port").getAsInt());
        }
        return desc;
    }
}
