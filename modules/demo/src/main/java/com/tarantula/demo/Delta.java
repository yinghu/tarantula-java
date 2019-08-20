package com.tarantula.demo;

import com.google.gson.*;
import com.tarantula.*;
import com.tarantula.Module;

import java.io.InputStream;
import java.io.InputStreamReader;

public class Delta implements Module {

    private JsonElement game;
    private PostOffice postOffice;
    private ApplicationContext context;
    private GsonBuilder builder;

    private long delta = 50;
    //private long noticeInterval = 500;
    private String action = "presence";
    private Timer timer;
    private DataStore dataStore;
    public void onJoin(Session session) throws Exception{
        byte[] ret = this.builder.create().toJson(game).getBytes();
        session.write(ret,this.label());
    }

    @Override
    public boolean onRequest(Session session, byte[] payload, OnUpdate update) throws Exception{
        boolean leaving = false;
        context.log("test update from module->"+session.action(),OnLog.INFO);
        postOffice.onMessage(session.systemId(),this.context.onRegistry().distributionKey(),payload);
        if(session.action().equals("a")){
            action = "presence";
            JsonObject jo = toPayload(action,"presence");
            byte[] ret = this.builder.create().toJson(jo).getBytes();
            session.write(ret,this.label());
            update.on(ret);
        }
        else if(session.action().equals("b")){
            action = "connection";
            JsonObject jo = toPayload(action,"connection");
            byte[] ret = this.builder.create().toJson(jo).getBytes();
            session.write(ret,this.label());
            update.on(ret);
        }
        else if(session.action().equals("c")){
            action = "game";
            byte[] ret = this.builder.create().toJson(toPayload(action,"game")).getBytes();
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
        this.builder = new GsonBuilder();
        this.builder.registerTypeAdapter(Timer.class,new TimerSerializer());
        this.timer = new Timer(60*1000,delta);
        timer.distributionKey(this.context.onRegistry().distributionKey());
        this.dataStore = this.context.dataStore("demo");
        dataStore.createIfAbsent(timer,true);
        timer.dataStore(dataStore);
        this.postOffice = this.context.postOffice();
        this.context.resource("demo.json",(InputStream in)->{
            InputStreamReader jr = new InputStreamReader(in);
            this.game = new JsonParser().parse(jr);
        });
        JsonObject jo = this.game.getAsJsonObject();
        jo.addProperty("responseLabel",this.label());
        jo.addProperty("successful",true);
        jo.addProperty("instanceId",context.onRegistry().distributionKey());

        //RecoverableListener r = dataStore.registerRecoverableListener(new DemoPortableRegistry());
        //r.addRecoverableFilter(DemoPortableRegistry.TIMER_OID,(c)->{
            //this.context.log(c.toString(),OnLog.INFO);
        //});
    }
    private JsonObject toPayload(String action,String label){
        JsonObject payload = game.getAsJsonObject();
        if(action.equals("game")){
            payload.addProperty("label",label);
            return payload;
        }
        payload = game.getAsJsonObject().getAsJsonObject(action);
        JsonObject jo = new JsonObject();
        jo.addProperty("label",label);
        JsonArray ja = new JsonArray();
        for(int i=0;i<10;i++){
            ja.add(payload);
        }
        jo.add("list",ja);
        return jo;
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
