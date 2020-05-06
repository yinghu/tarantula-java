package com.tarantula.game;

import com.google.gson.JsonObject;
import com.tarantula.Connection;
import com.tarantula.platform.ResponseHeader;
import com.tarantula.platform.util.ConnectionSerializer;

/**
 * Created by yinghu lu on 4/14/2020.
 */
public class GameObject extends ResponseHeader {

    public Stub stub;
    public Connection connection;
    public String ticket;
    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("successful",successful);
        if(!successful){
            jo.addProperty("message",message);
        }
        jo.addProperty("ticket",ticket);
        jo.add("stub",stub.toJson());
        if(connection!=null){
            JsonObject jp = new JsonObject();
            jp.addProperty("type",connection.type());
            jp.addProperty("serverId",connection.serverId());
            jp.addProperty("secured",connection.secured());
            jp.addProperty("protocol",connection.protocol());
            jp.addProperty("subProtocol",connection.subProtocol());
            jp.addProperty("host",connection.host());
            jp.addProperty("path",connection.path());
            jp.addProperty("port",connection.port());
            jo.add("connection",jp);
        }
        return jo;
    }
}
