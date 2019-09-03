package com.tarantula.test.integration;

import com.google.gson.JsonObject;
import java.net.http.WebSocket;
import java.nio.CharBuffer;
public class DemoSync extends OnGame {


    public DemoSync(){
        super();
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
            onStream(webSocket);
            onAction(webSocket,data->{data.addProperty("command","a");data.addProperty("timestamp",System.currentTimeMillis());});
            onAction(webSocket,data->{data.addProperty("command","b");data.addProperty("timestamp",System.currentTimeMillis());});
            onAction(webSocket,data->{data.addProperty("command","c");data.addProperty("timestamp",System.currentTimeMillis());});
            Thread.sleep(1000);
            onAction(caller,data ->data.addProperty("command","onLeave"));
            System.out.println("Total Bytes Received ["+totalBytesReceived.get()+"]");
        }catch (Exception ex){

        }
    }
    public void onMessage(CharSequence message){
        super.onMessage(message);
        if(message.charAt(4)=='{'){
            System.out.println(message.subSequence(4,message.length()));
        }
    }
}
