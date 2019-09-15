package com.tarantula.demo;

import com.google.gson.*;
import com.tarantula.*;
import com.tarantula.Module;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.util.OnAccessDeserializer;


public class Boost implements Module {


    private PostOffice postOffice;
    private ApplicationContext context;
    private GsonBuilder builder;

    private long delta = 50;
    //private long noticeInterval = 500;
    private Timer timer;
    private Statistics statistics;
    private DataStore dataStore;
    public void onJoin(Session session,Connection connection) throws Exception{
        if(connection!=null){
            //this.context.log(connection.type()+"/"+connection.serverId(),OnLog.INFO);
            this.statistics.value("playerCount",1);
            DemoObject dj = this.demoObject("onJoin",System.currentTimeMillis(),connection,this.context.validator().ticket(session.systemId(),session.stub()));
            byte[] ret = this.builder.create().toJson(dj).getBytes();
            session.write(ret,this.label());
        }else{
            this.statistics.value("playerCount",1);
            byte[] ret = this.builder.create().toJson(this.demoObject("onJoin",System.currentTimeMillis())).getBytes();
            session.write(ret,this.label());
        }
    }

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception{
        boolean leaving = false;
        OnAccess onAccess = this.builder.create().fromJson(new String(payload),OnAccess.class);
        //context.log("test update from module->"+session.action()+"/"+onAccess.timestamp(),OnLog.INFO);
        if(session.action().equals("a")){
            byte[] ret = this.builder.create().toJson(this.demoObject(session.action(),onAccess.timestamp())).getBytes();
            session.write(ret,this.label());
            update.on(ret);
            postOffice.onTopic().send("presence/notice",ret);
            this.context.onRegistry().transact(session.systemId(),1000);
            OnStatistics delta = this.context.statistics().value("WonCount",1000);
            delta.xpDelta(1000);
            delta.owner(session.systemId());
            delta.onEntry("WonCount",1000);
            this.postOffice.onTag(Level.LEVEL_TAG).send(delta.owner(),delta);
        }
        else if(session.action().equals("b")){
            byte[] ret = this.builder.create().toJson(this.demoObject(session.action(),onAccess.timestamp())).getBytes();
            session.write(ret,this.label());
            update.on(ret);
            OnStatistics delta = this.context.statistics().value("WagerCount",1000);
            delta.xpDelta(1000);
            delta.owner(session.systemId());
            delta.onEntry("WagerCount",1000);
            this.postOffice.onTag(Level.LEVEL_TAG).send(delta.owner(),delta);
            this.context.onRegistry().transact(session.systemId(),2000);
        }
        else if(session.action().equals("c")){
            byte[] ret = this.builder.create().toJson(this.demoObject(session.action(),onAccess.timestamp())).getBytes();
            session.write(ret,this.label());
            update.on(ret);
            OnStatistics delta = this.context.statistics().value("BlackJackCount",1000);
            delta.xpDelta(1000);
            delta.owner(session.systemId());
            delta.onEntry("BlackJackCount",1000);
            this.postOffice.onTag(Level.LEVEL_TAG).send(delta.owner(),delta);
            this.context.onRegistry().transact(session.systemId(),4000);
        }
        else if(session.action().equals("onLeave")){
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
        this.builder.registerTypeAdapter(DemoObject.class,new DemoObjectSerializer());
        this.builder.registerTypeAdapter(Timer.class,new TimerSerializer());
        this.builder.registerTypeAdapter(OnAccess.class,new OnAccessDeserializer());
        this.timer = new Timer(60*1000,delta);
        this.timer.distributionKey(this.context.onRegistry().distributionKey());
        this.statistics = this.context.statistics();
        this.dataStore = this.context.dataStore("demo");
        dataStore.createIfAbsent(timer,true);
        this.context.registerRecoverableListener(new PresencePortableRegistry()).addRecoverableFilter(PresencePortableRegistry.ON_BALANCE_CID,(t)->{
            OnBalance ob = (OnBalance)t;
            this.context.onRegistry().transact(ob.owner(),ob.balance());
        });
    }

    public void onTimer(OnUpdate update){
        delta -= this.context.descriptor().timerOnModule();
        if(delta<=0){
            Timer tuu = timer.update();
            update.on(this.builder.create().toJson(tuu).getBytes());
            ///postOffice.onTopic().send("presence/notice",this.builder.create().toJson(timer).getBytes());
            delta = 50;
        }
    }
    public void clear(){
        this.dataStore.update(timer);
        this.context.log("sync->"+this.context.onRegistry().distributionKey(),OnLog.WARN);
    }
    private DemoObject demoObject(String command,long timestamp,Connection connection,String ticket){
        DemoObject mo = new DemoObject(command,this.statistics,this.timer.update(),connection,ticket);
        mo.timestamp(timestamp);
        mo.instanceId(this.context.onRegistry().distributionKey());
        mo.name("Boost");
        mo.label(this.label());
        return mo;
    }
    private DemoObject demoObject(String command,long timestamp){
        DemoObject mo = new DemoObject(command,this.statistics,this.timer.update());
        mo.timestamp(timestamp);
        mo.instanceId(this.context.onRegistry().distributionKey());
        mo.name("Boost");
        mo.label(this.label());
        return mo;
    }

}
