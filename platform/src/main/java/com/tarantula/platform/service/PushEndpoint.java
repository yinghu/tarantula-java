package com.tarantula.platform.service;

import com.google.gson.JsonObject;
import com.tarantula.platform.UniverseConnection;

public class PushEndpoint extends UniverseConnection {

    public PushEndpoint(String host,int port){
        this.host = host;
        this.port = port;
    }
    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("ticket",(String)properties.get("ticket"));
        jo.addProperty("host",host);
        jo.addProperty("port",port);
        jo.addProperty("successful",successful);
        return jo;
    }
}
