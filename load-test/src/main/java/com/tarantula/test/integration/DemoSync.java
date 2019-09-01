package com.tarantula.test.integration;

import com.google.gson.JsonObject;
import java.net.http.WebSocket;

public class DemoSync extends OnGame {


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
            onAction(webSocket,data->data.addProperty("command","a"));
            onAction(webSocket,data->data.addProperty("command","b"));
            onAction(webSocket,data->data.addProperty("command","c"));
            Thread.sleep(3000);
            onAction(caller,data ->data.addProperty("command","onLeave"));

        }catch (Exception ex){

        }
    }
    public void onMessage(CharSequence message){
        //System.out.println(message);
    }
}
