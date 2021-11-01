package com.tarantula.platform.util;

import com.google.gson.*;
import com.icodesoftware.Connection;

import java.lang.reflect.Type;

public class ConnectionSerializer implements JsonSerializer<Connection> {

    @Override
    public JsonElement serialize(Connection connection, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type",connection.type());
        jsonObject.addProperty("serverId",connection.serverId());
        jsonObject.addProperty("connectionId",connection.connectionId());
        jsonObject.addProperty("secured",connection.secured());
        jsonObject.addProperty("protocol",connection.protocol());
        jsonObject.addProperty("subProtocol",connection.subProtocol());
        jsonObject.addProperty("host",connection.host());
        jsonObject.addProperty("port",connection.port());
        jsonObject.addProperty("path",connection.path());
        return jsonObject;
    }
}
