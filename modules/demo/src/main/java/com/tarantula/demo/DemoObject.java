package com.tarantula.demo;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tarantula.Statistics;
import com.tarantula.platform.OnApplicationHeader;
import com.tarantula.platform.util.StatisticsSerializer;

import java.lang.reflect.Type;

public class DemoObject extends OnApplicationHeader {

    private Timer timer;
    private Statistics statistics;

    public DemoObject(String command,Statistics statistics,Timer timer){
        this.successful = true;
        this.command = command;
        this.statistics = statistics;
        this.timer = timer;
    }

    public JsonElement setup(Type type, JsonSerializationContext jsonSerializationContext){
        JsonObject jsonObject = new JsonObject();
        if(timer!=null){
            jsonObject.add("timer",new TimerSerializer().serialize(timer,type,jsonSerializationContext));
        }
        if(statistics!=null){
            jsonObject.add("statistics",new StatisticsSerializer().serialize(statistics,type,jsonSerializationContext));
        }
        return jsonObject;
    }
}
