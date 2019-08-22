package com.tarantula.demo;

import com.google.gson.*;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.game.GameObject;
import com.tarantula.game.GameObjectSerializer;

import java.lang.reflect.Type;

public class Boost extends GameObject implements Module {


    private PostOffice postOffice;
    private ApplicationContext context;
    private GsonBuilder builder;

    private long delta = 50;
    //private long noticeInterval = 500;
    private Timer timer;
    private DataStore dataStore;
    public void onJoin(Session session) throws Exception{
        byte[] ret = this.builder.create().toJson(this).getBytes();
        session.write(ret,this.label());
    }

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception{
        boolean leaving = false;
        context.log("test update from module->"+session.action(),OnLog.INFO);
        postOffice.onMessage(session.systemId(),this.context.onRegistry().distributionKey(),payload);
        if(session.action().equals("a")){
            byte[] ret = this.builder.create().toJson(timer).getBytes();
            session.write(ret,this.label());
            update.on(ret);
            RoutingKey rk = this.context.routingKey(session.systemId(),"demo/service");
            //this.context.publish();
        }
        else if(session.action().equals("b")){
            byte[] ret = this.builder.create().toJson(timer).getBytes();
            session.write(ret,this.label());
            update.on(ret);
        }
        else if(session.action().equals("c")){
            byte[] ret = this.builder.create().toJson(timer).getBytes();
            session.write(ret,this.label());
            update.on(ret);
        }
        else if(session.action().equals("onLeave")){
            session.write(payload,this.label());
            leaving = true;
        }
        else{
            session.write(payload,this.label());
            leaving = true;
        }
        return leaving;
    }
    public String label(){
        return "demo";
    }
    public void setup(ApplicationContext context) throws Exception{
        this.context = context;
        this.postOffice = this.context.postOffice();
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(Boost.class,new GameObjectSerializer());
        this.builder.registerTypeAdapter(Timer.class,new TimerSerializer());
        this.timer = new Timer(60*1000,delta);
        this.timer.distributionKey(this.context.onRegistry().distributionKey());
        this.dataStore = this.context.dataStore("demo");
        dataStore.createIfAbsent(timer,true);
        timer.dataStore(dataStore);
        this.name("boost");
        this.instanceId(context.onRegistry().distributionKey());
        this.successful(true);
    }
    @Override
    public synchronized JsonElement setup(Type type, JsonSerializationContext jsonSerializationContext){
        return new TimerSerializer().serialize(this.timer,type,jsonSerializationContext);
    }
    public void onTimer(OnUpdate update){
        timer.onUpdate();
        update.on(this.builder.create().toJson(timer).getBytes());
    }
    public void clear(){
        this.dataStore.update(timer);
        this.context.log("sync->"+this.context.onRegistry().distributionKey(),OnLog.WARN);
    }

}
