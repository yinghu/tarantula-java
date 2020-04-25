package com.tarantula.admin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tarantula.ApplicationContext;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

public class AdminDataStoreObject extends AdminObject {


    public ConcurrentHashMap<String,Long> kv = new ConcurrentHashMap<>();

    public AdminDataStoreObject(String message,String label){
        super(label);
        this.message = message;
    }
    public JsonElement setup(Type type, JsonSerializationContext jsonSerializationContext){
        JsonObject jo = super.setup(type,jsonSerializationContext).getAsJsonObject();
        kv.forEach((k,v)->{
            jo.addProperty(k,v);
        });
        return jo;
    }
    public void reset(ApplicationContext context){
        long total =0;
        this.kv.put("Tarantula",context.dataStore("tarantula").count());
        total += this.kv.get("Tarantula");
        this.kv.put("User",context.dataStore("user").count());
        total += this.kv.get("User");
        this.kv.put("Presence",context.dataStore("presence").count());
        total += this.kv.get("Presence");
        this.kv.put("Level",context.dataStore("level").count());
        total += this.kv.get("Level");
        this.kv.put("LeaderBoard",context.dataStore("leaderBoard").count());
        total += this.kv.get("LeaderBoard");
        long ai = 0;
        for(int i=0;i<271;i++){
            ai += context.dataStore("p"+i).count();//only works on cache
        }
        this.kv.put("AccessIndex",ai);
        total += this.kv.get("AccessIndex");
        this.kv.put("Total",total);
    }
}
