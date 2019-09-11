package com.tarantula.demo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tarantula.Connection;
import com.tarantula.Statistics;
import com.tarantula.platform.OnApplicationHeader;
import com.tarantula.platform.util.ConnectionSerializer;
import com.tarantula.platform.util.StatisticsSerializer;

import java.lang.reflect.Type;

public class DemoObject extends OnApplicationHeader {

    private Timer timer;
    private Statistics statistics;
    private Connection connection;
    private String ticket;
    public DemoObject(String command,Statistics statistics,Timer timer){
        this.successful = true;
        this.command = command;
        this.statistics = statistics;
        this.timer = timer;
    }
    public DemoObject(String command,Statistics statistics,Timer timer,Connection connection,String ticket){
        this.successful = true;
        this.command = command;
        this.statistics = statistics;
        this.timer = timer;
        this.connection = connection;
        this.ticket = ticket;
    }

    public JsonElement setup(Type type, JsonSerializationContext jsonSerializationContext){
        JsonObject jsonObject = new JsonObject();
        if(timer!=null){
            jsonObject.add("timer",new TimerSerializer().serialize(timer,type,jsonSerializationContext));
        }
        if(statistics!=null){
            jsonObject.add("statistics",new StatisticsSerializer().serialize(statistics,type,jsonSerializationContext));
        }
        if(connection!=null){
            jsonObject.add("connection",new ConnectionSerializer().serialize(connection,type,jsonSerializationContext));
            jsonObject.addProperty("ticket",ticket);
        }
        return jsonObject;
    }
}
