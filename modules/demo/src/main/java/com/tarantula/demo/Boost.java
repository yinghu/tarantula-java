package com.tarantula.demo;

import com.google.gson.*;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.game.GameObject;
import com.tarantula.game.GameObjectSerializer;
import com.tarantula.platform.presence.PresencePortableRegistry;

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
        if(session.action().equals("a")){
            byte[] ret = this.builder.create().toJson(timer).getBytes();
            session.write(ret,this.label());
            update.on(ret);
            postOffice.onLabel().send("presence/notice",ret);
            this.context.onRegistry().transact(session.systemId(),1000);
            OnStatistics delta = this.context.statistics().value("WonCount",1000);
            //context.log("test update from module->"+delta.name(),OnLog.INFO);
            delta.xpDelta(1000);
            delta.owner(session.systemId());
            delta.onEntry("WonCount",1000);
            this.postOffice.onTag(Level.LEVEL_TAG).send(delta.owner(),delta);
        }
        else if(session.action().equals("b")){
            byte[] ret = this.builder.create().toJson(timer).getBytes();
            session.write(ret,this.label());
            update.on(ret);
            this.context.onRegistry().transact(session.systemId(),2000);
        }
        else if(session.action().equals("c")){
            byte[] ret = this.builder.create().toJson(timer).getBytes();
            session.write(ret,this.label());
            update.on(ret);
            this.context.onRegistry().transact(session.systemId(),4000);
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
        this.name("boost");
        this.instanceId(context.onRegistry().distributionKey());
        this.successful(true);
        this.context.registerRecoverableListener(new PresencePortableRegistry()).addRecoverableFilter(PresencePortableRegistry.ON_BALANCE_CID,(t)->{
            OnBalance ob = (OnBalance)t;
            this.context.onRegistry().transact(ob.owner(),ob.balance());
        });
    }
    @Override
    public synchronized JsonElement setup(Type type, JsonSerializationContext jsonSerializationContext){
        return new TimerSerializer().serialize(this.timer,type,jsonSerializationContext);
    }
    public void onTimer(OnUpdate update){
        timer.update();
        update.on(this.builder.create().toJson(timer).getBytes());
    }
    public void clear(){
        this.dataStore.update(timer);
        this.context.log("sync->"+this.context.onRegistry().distributionKey(),OnLog.WARN);
    }

}
