package com.tarantula.test.integration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.http.WebSocket;

public class DemoSync extends OnGame {

    private JsonParser parser;
    public DemoSync(){
        super();
        parser = new JsonParser();
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
            this.presence = presence;
            this.applicationId = joined.get("applicationId").getAsString();
            this.instanceId = joined.get("instanceId").getAsString();
            long waiting = 4000;
            onStream(webSocket);
            for(int i=0;i<10;i++){
                onAction(webSocket,data->{data.addProperty("command","a");data.addProperty("timestamp",System.currentTimeMillis());});
                Thread.sleep(waiting);
                onAction(webSocket,data->{data.addProperty("command","b");data.addProperty("timestamp",System.currentTimeMillis());});
                Thread.sleep(waiting);
                onAction(webSocket,data->{data.addProperty("command","c");data.addProperty("timestamp",System.currentTimeMillis());});
                Thread.sleep(waiting);
            }
            Thread.sleep(5000);
            onAction(caller,data ->data.addProperty("command","onLeave"));
            System.out.println(LoadResult.print());
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void onMessage(CharSequence message){
        super.onMessage(message);
        if(message.charAt(4)=='{'){
            JsonObject jo = parser.parse(message.subSequence(4,message.length()).toString()).getAsJsonObject();
            if(jo.has("command")){
                String cmd = jo.get("command").getAsString();
                if(cmd.equals("a")||cmd.equals("b")||cmd.equals("")){
                    long dur = (System.currentTimeMillis()-jo.get("timestamp").getAsLong());
                    if(dur<=10){
                        LoadResult.totalRoundTrip1_10.incrementAndGet();
                    }
                    else if(dur>10&&dur<=50){
                        LoadResult.totalRoundTrip11_50.incrementAndGet();
                    }
                    else if(dur>50&&dur<=100){
                        LoadResult.totalRoundTrip51_100.incrementAndGet();
                    }
                    else if(dur>100&&dur<=500){
                        LoadResult.totalRoundTrip101_500.incrementAndGet();
                    }
                    else{
                        LoadResult.totalRoundTripMore500.incrementAndGet();
                        System.out.println(dur);
                    }
                }
            }
        }
    }
}
