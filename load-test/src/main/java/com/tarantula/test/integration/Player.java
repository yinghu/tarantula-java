package com.tarantula.test.integration;

import com.google.gson.JsonObject;
import com.tarantula.Session;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.HashMap;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

public class Player implements Runnable, WebSocket.Listener{

    private boolean secure;
    private String host;
    private CountDownLatch counter;
    private HashMap<String,String> _headers = new HashMap<>();
    private String userName;
    private HTTPCaller.OnResponse done;
    private boolean[] continuing = {false};
    private JsonObject presence;
    private JsonObject connection;
    private WebSocket webSocket;
    private CountDownLatch waiting;


    public Player(boolean secure, String host, CountDownLatch counter,String userName, HTTPCaller.OnResponse done){
        this.secure = secure;
        this.host = host;
        this.counter = counter;
        this.userName = userName;
        this.done = done;
        waiting = new CountDownLatch(1);
    }
    private boolean isContinue(JsonObject json){
         continuing[0]= json.get("successful").getAsBoolean();
         if(!continuing[0]){
             done.on(json);
         }
         return continuing[0];
    }
    public void run() {
        long st = System.currentTimeMillis();
        try{
            HTTPCaller caller = new HTTPCaller(secure,host);
            _headers.put(Session.TARANTULA_TAG,"index/lobby");
            caller.doAction("user/index","onIndex",_headers,null,(json -> {
                isContinue(json);
            }));
            if(continuing[0]){
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("nickname","Player");
                jsonObject.addProperty("password","password");
                jsonObject.addProperty("login",userName);
                _headers.clear();
                _headers.put(Session.TARANTULA_TAG,"index/user");
                _headers.put(Session.TARANTULA_MAGIC_KEY,jsonObject.get("login").getAsString());
                caller.doAction("user/action","onRegister",_headers,jsonObject.toString().getBytes(),json -> {
                    isContinue(json);
                });
            }
            if(continuing[0]){
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("password","password");
                jsonObject.addProperty("login",userName);
                _headers.clear();
                _headers.put(Session.TARANTULA_TAG,"index/user");
                _headers.put(Session.TARANTULA_MAGIC_KEY,jsonObject.get("login").getAsString());
                caller.doAction("user/action","onLogin",_headers,jsonObject.toString().getBytes(),json -> {
                    if(isContinue(json)){
                        //System.out.println(json.get("presence"));
                        presence = json.get("presence").getAsJsonObject();
                    }
                });
            }
            if(continuing[0]){
                _headers.clear();
                _headers.put(Session.TARANTULA_TAG,"presence/lobby");
                _headers.put(Session.TARANTULA_TOKEN,presence.get("token").getAsString());
                caller.doAction("service/action","onPresence",_headers,"{}".getBytes(),json -> {
                    if(isContinue(json)){
                        //System.out.println(json.get("connection"));
                        connection = json.get("connection").getAsJsonObject();
                    }
                });
            }
            if(continuing[0]){
                onWebSocket();
            }
            if(continuing[0]){
                //onGame and play
            }
            if(continuing[0]){
                waiting.await();
                offWebSocket();
                _headers.clear();
                _headers.put(Session.TARANTULA_TAG,"presence/lobby");
                _headers.put(Session.TARANTULA_TOKEN,presence.get("token").getAsString());
                caller.doAction("service/action","onAbsence",_headers,"{}".getBytes(),json -> {
                    if(isContinue(json)){
                        //System.out.println(json);
                        done.on(json);
                    }
                });

            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        finally {
            System.out.println((System.currentTimeMillis()-st));
            counter.countDown();
        }
    }

    private void onWebSocket() throws Exception{
        StringBuffer sb = new StringBuffer(connection.get("protocol").getAsString());
        sb.append("://").append(connection.get("host").getAsString()).append(":").append(connection.get("port").getAsInt()).append("/").append(connection.get("path").getAsString());
        URI uri = new URI(sb.toString()+"?accessKey="+ URLEncoder.encode(presence.get("ticket").getAsString(),"utf-8")+"&stub="+presence.get("stub").getAsInt()+"&systemId="+presence.get("login").getAsString());
        webSocket = HttpClient.newHttpClient().newWebSocketBuilder().header("Origin","http://localhost:8090").subprotocols("tarantula-service").buildAsync(uri,this).join();
        JsonObject jo = new JsonObject();
        jo.addProperty("action","onStart");
        jo.addProperty("streaming",true);
        jo.addProperty("label","presence/notice");
        JsonObject jd = new JsonObject();
        jd.addProperty("command","onStart");
        jo.add("data",jd);
        System.out.println(jo.toString());
        webSocket.sendText(jo.toString(),true);
        Thread.sleep(5000);
        waiting.countDown();
        //webSocket.sendClose(WebSocket.NORMAL_CLOSURE,"closed");
    }
    private void offWebSocket() throws Exception{
        JsonObject jo = new JsonObject();
        jo.addProperty("action","onStop");
        jo.addProperty("streaming",true);
        jo.addProperty("label","presence/notice");
        JsonObject jd = new JsonObject();
        jd.addProperty("command","onStop");
        jo.add("data",jd);
        System.out.println(jo.toString());
        webSocket.sendText(jo.toString(),true);
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE,"closed");
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        WebSocket.Listener.super.onOpen(webSocket);
        continuing[0]=true;
    }

    @Override
    public void onError(WebSocket ws, Throwable error) {
        error.printStackTrace();
        continuing[0]=false;
        WebSocket.Listener.super.onError(ws, error);
    }

    @Override
    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
        //start.compareAndSet(0,System.currentTimeMillis());
        //end.set(System.currentTimeMillis());
        //totalBytes.addAndGet(data.length()*8);
        System.out.println(data);
        return WebSocket.Listener.super.onText(ws, data, last);
    }
}
