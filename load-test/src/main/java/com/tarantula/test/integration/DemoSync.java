package com.tarantula.test.integration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.WebSocket;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class DemoSync extends OnGame {

    private JsonParser parser;
    private Semaphore semaphore;
    private AtomicInteger ct;
    public DemoSync(){
        super();
        parser = new JsonParser();
        semaphore = new Semaphore(1);
        ct = new AtomicInteger(0);
    }

    @Override
    public String typeId() {
        return "demo";
    }

    @Override
    public void onPlay(JsonObject joined, WebSocket webSocket,HTTPCaller caller,JsonObject presence){
        try{
            if(!joined.get("successful").getAsBoolean()){
                return;
            }
            semaphore.acquire();
            ct.set(3);
            this.presence = presence;
            this.applicationId = joined.get("applicationId").getAsString();
            this.instanceId = joined.get("instanceId").getAsString();
            onStream(webSocket);
            onAction(webSocket,data->{data.addProperty("command","a");data.addProperty("timestamp",System.currentTimeMillis());});
            onAction(webSocket,data->{data.addProperty("command","b");data.addProperty("timestamp",System.currentTimeMillis());});
            onAction(webSocket,data->{data.addProperty("command","c");data.addProperty("timestamp",System.currentTimeMillis());});
            semaphore.acquire();
            Thread.sleep(100);
            onAction(caller,data ->data.addProperty("command","onLeave"));
            semaphore.release();
            System.out.println("Total Bytes Received ["+totalBytesReceived.get()+"]");
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void onMessage(CharSequence message){
        super.onMessage(message);
        try{
        if(message.charAt(4)=='{'){
            JsonObject jo = this.parser.parse(message.subSequence(4,message.length()).toString()).getAsJsonObject();
            String cmd = jo.get("command").getAsString();
            if(cmd.equals("a")||cmd.equals("b")||cmd.equals("c")){
                if(ct.decrementAndGet()==0){
                    semaphore.release();
                }
            }
        }}catch (Exception ex){
            ex.printStackTrace();
        }
        //System.out.println(message);
    }
}
