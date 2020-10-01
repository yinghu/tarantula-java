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
        jsonObject.addProperty("sequence",connection.sequence());
        jsonObject.addProperty("secured",connection.secured());
        jsonObject.addProperty("protocol",connection.protocol());
        jsonObject.addProperty("subProtocol",connection.subProtocol());
        jsonObject.addProperty("host",connection.host());
        jsonObject.addProperty("port",connection.port());
        jsonObject.addProperty("path",connection.path());
        jsonObject.addProperty("maxConnections",connection.maxConnections());

        if(connection.server()!=null){
            JsonObject server = new JsonObject();
            Connection sc = connection.server();
            server.addProperty("type",sc.type());
            server.addProperty("serverId",sc.serverId());
            server.addProperty("secured",sc.secured());
            server.addProperty("protocol",sc.protocol());
            server.addProperty("subProtocol",sc.subProtocol());
            server.addProperty("host",sc.host());
            server.addProperty("port",sc.port());
            server.addProperty("path",sc.path());
            server.addProperty("maxConnections",sc.maxConnections());
            jsonObject.add("server",server);
        }
        return jsonObject;
    }
}
