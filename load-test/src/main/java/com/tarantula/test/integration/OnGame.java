package com.tarantula.test.integration;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.tarantula.test.HTTPCaller;

import java.net.http.WebSocket;
import java.util.HashMap;

public class OnGame {

    protected String applicationId;
    protected String instanceId;
    protected JsonObject presence;
    protected HashMap<String,String> _headers = new HashMap<>();

    public OnGame(){

    }
    public String typeId(){
        return null;
    }
    public void onPlay(JsonObject joined, WebSocket webSocket, HTTPCaller httpCaller, JsonObject presence){

    }
    public void onMessage(CharSequence message){
        LoadResult.totalBytesReceived.addAndGet(message.length());
    }
    protected void onAction(WebSocket webSocket,OnPayload onPayload){
        JsonObject data = new JsonObject();
        onPayload.on(data);
        JsonObject payload = new JsonObject();
        payload.add("data",data);
        payload.addProperty("path","/application/instance");
        payload.addProperty("applicationId",applicationId);
        payload.addProperty("instanceId",instanceId);
        payload.addProperty("action",data.get("command").getAsString());
        payload.addProperty("streaming",false);
        webSocket.sendText(payload.toString(),true);
    }
    protected void onStream(WebSocket webSocket){
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
    protected void onAction(HTTPCaller caller,OnPayload onPayload){
        JsonObject jo = new JsonObject();
        onPayload.on(jo);
        _headers.put(Session.TARANTULA_TOKEN,presence.get("token").getAsString());
        //_headers.put(Session.TARANTULA_APPLICATION_ID,applicationId);
        //_headers.put(Session.TARANTULA_INSTANCE_ID,instanceId);
        caller.doAction("application/instance",jo.get("command").getAsString(),_headers,jo.toString().getBytes(),resp -> onMessage(resp.toString()));
    }
}
