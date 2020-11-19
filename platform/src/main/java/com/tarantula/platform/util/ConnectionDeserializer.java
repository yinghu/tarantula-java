package com.tarantula.platform.util;

import com.google.gson.*;

import com.icodesoftware.Connection;
import com.tarantula.platform.UniverseConnection;
import java.lang.reflect.Type;

public class ConnectionDeserializer implements JsonDeserializer<Connection> {

    @Override
    public Connection deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        Connection desc = toConnection(jo);
        if(jo.has("server")){
            JsonObject server = jo.getAsJsonObject("server");
            Connection scc = toConnection(server);
            scc.messageId(desc.messageId());
            scc.messageIdOffset(desc.messageIdOffset());
            desc.server(scc);
        }
        return desc;
    }
    private Connection toConnection(JsonObject jo){
        Connection desc = new UniverseConnection();
        if(jo.has("type")){
            desc.type(jo.get("type").getAsString());
        }
        if(jo.has("serverId")){
            desc.serverId(jo.get("serverId").getAsString());
        }
        if(jo.has("connectionId")){
            desc.connectionId(jo.get("connectionId").getAsLong());
        }
        if(jo.has("sequence")){
            desc.sequence(jo.get("sequence").getAsInt());
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
        if(jo.has("messageId")){
            desc.messageId(jo.get("messageId").getAsInt());
        }
        if(jo.has("messageIdOffset")){
            desc.messageIdOffset(jo.get("messageIdOffset").getAsInt());
        }
        return desc;
    }
}
