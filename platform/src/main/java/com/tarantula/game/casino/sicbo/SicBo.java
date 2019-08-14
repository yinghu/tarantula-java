package com.tarantula.game.casino.sicbo;

import com.tarantula.ApplicationContext;
import com.tarantula.game.Game;

import java.util.concurrent.ConcurrentHashMap;

public class SicBo extends Game {

    public ConcurrentHashMap<String,Long> kv = new ConcurrentHashMap<>();
    public ApplicationContext context;
    public SicBo(){
        this.label = "sicbo";
        this.duration = 1000*Game.CHECK_POINT_INTERVAL;
    }
    public void join(String systemId){}
    public SicBo setup(){return  this;}

    public void reset(){
        long total =0;
        this.kv.put("Tarantula",this.context.dataStore("tarantula").count());
        total += this.kv.get("Tarantula");
        this.kv.put("User",this.context.dataStore("user").count());
        total += this.kv.get("User");
        this.kv.put("Profile",this.context.dataStore("profile").count());
        total += this.kv.get("Profile");
        this.kv.put("Presence",this.context.dataStore("presence").count());
        total += this.kv.get("Presence");
        this.kv.put("Session",this.context.dataStore("session").count());
        total += this.kv.get("Session");
        this.kv.put("Level",this.context.dataStore("level").count());
        total += this.kv.get("Level");
        long ai = 0;
        for(int i=0;i<271;i++){
            ai +=this.context.dataStore("p"+i).count();//only works on cache
        }
        this.kv.put("AccessIndex",ai);
        total += this.kv.get("AccessIndex");
        this.kv.put("Total",total);
        pendingQueue.offer(this);
    }
}
