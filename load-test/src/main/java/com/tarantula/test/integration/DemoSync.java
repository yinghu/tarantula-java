package com.tarantula.test.integration;

import com.google.gson.JsonObject;
import com.tarantula.Session;

import java.net.http.WebSocket;
import java.util.HashMap;

public class DemoSync implements OnGame {

    private String applicationId;
    private String instanceId;
    private JsonObject presence;
    private HashMap<String,String> _headers = new HashMap<>();
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
            applicationId = joined.get("applicationId").getAsString();
            instanceId = joined.get("instanceId").getAsString();
            onStream(webSocket);
            Thread.sleep(10000);
            onLeave(caller);

        }catch (Exception ex){

        }
    }
    public void onMessage(CharSequence message){
        System.out.println(message);
    }
    private void onStream(WebSocket webSocket){
        JsonObject jo = new JsonObject();
        jo.addProperty("action","onStream");
        jo.addProperty("applicationId",applicationId);
        jo.addProperty("instanceId",instanceId);
        jo.addProperty("streaming",true);
        jo.addProperty("path","/application/instance");
        JsonObject data = new JsonObject();
        data.addProperty("command","onStream");
        jo.add("data",data);
        webSocket.sendText(jo.toString(),true);
    }
    private void onLeave(HTTPCaller caller){
        JsonObject jo = new JsonObject();
        jo.addProperty("command","onLeave");
        _headers.put(Session.TARANTULA_TOKEN,presence.get("token").getAsString());
        _headers.put(Session.TARANTULA_APPLICATION_ID,applicationId);
        _headers.put(Session.TARANTULA_INSTANCE_ID,instanceId);
        caller.doAction("application/instance","onLeave",_headers,jo.toString().getBytes(),resp -> onMessage(resp.toString()));
    }
}
