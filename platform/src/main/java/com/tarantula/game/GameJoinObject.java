package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.Connection;
import com.icodesoftware.Consumable;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.ResponseHeader;

import java.time.format.DateTimeFormatter;


public class GameJoinObject extends ResponseHeader {

    public Stub stub;
    public Connection connection;
    public String ticket;
    public String serverKey;
    public boolean offline;
    public boolean tournamentEnabled;
    public Consumable consumable;
    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("successful",successful);
        if(!successful){
            jo.addProperty("message",message);
            return jo;
        }
        jo.addProperty("offline",offline);
        jo.addProperty("tournamentEnabled",tournamentEnabled);
        if(ticket!=null){
            jo.addProperty("ticket",ticket);
        }
        if(serverKey!=null){
            jo.addProperty("serverKey",serverKey);
        }
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
        if(consumable!=null){
            JsonObject jc = consumable.toJson();
            jo.add("configurations",jc!=null?jc: JsonUtil.toJsonObject(consumable.toMap()));
        }
        return jo;
    }
}
