package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.Connection;
import com.tarantula.platform.ResponseHeader;

/**
 * Updated by yinghu lu on 12/11/2020.
 */
public class GameJoinObject extends ResponseHeader {

    public Stub stub;
    public Connection connection;
    public String ticket;
    public String serverKey;
    public boolean offline;
    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("successful",successful);
        if(!successful){
            jo.addProperty("message",message);
            return jo;
        }
        jo.addProperty("offline",offline);
        jo.addProperty("ticket",ticket);
        jo.addProperty("serverKey",serverKey);
        jo.add("stub",stub.toJson());
        if(connection!=null){
            JsonObject jp = new JsonObject();
            jp.addProperty("type",connection.type());
            jp.addProperty("serverId",connection.serverId());
            jp.addProperty("secured",connection.secured());
            jp.addProperty("connectionId",connection.connectionId());
            jp.addProperty("host",connection.host());
            jp.addProperty("port",connection.port());
            jo.add("connection",jp);
        }
        return jo;
    }
}
