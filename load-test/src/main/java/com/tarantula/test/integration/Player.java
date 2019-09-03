package com.tarantula.test.integration;

import com.google.gson.JsonObject;
import com.tarantula.Session;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;

public class Player implements Runnable, WebSocket.Listener{

    private boolean secure;
    private String host;
    private CountDownLatch counter;
    private HashMap<String,String> _headers = new HashMap<>();
    private String userName;
    private OnPayload done;
    private boolean[] continuing = {false};
    private JsonObject presence;
    private JsonObject connection;
    private WebSocket webSocket;
    private CountDownLatch waiting;

    private OnGame onGame;
    private long start;
    private JsonObject gameLobby;
    private StringBuilder dataBuffer;
    CompletableFuture<?> accumulatedMessage;
    public Player(boolean secure, String host, CountDownLatch counter, String userName, OnGame onGame, OnPayload done){
        this.secure = secure;
        this.host = host;
        this.counter = counter;
        this.userName = userName;
        this.onGame = onGame;
        this.done = done;
        waiting = new CountDownLatch(1);
        this.dataBuffer = new StringBuilder();
        this.accumulatedMessage = new CompletableFuture<>();
    }
    private boolean isContinue(JsonObject json){
         continuing[0]= json.get("successful").getAsBoolean();
         if(!continuing[0]){
             json.addProperty("duration",(System.currentTimeMillis()-start));
             done.on(json);
         }
         this.onGame.onMessage(json.toString());
         return continuing[0];
    }
    public void run() {
        start = System.currentTimeMillis();
        try{
            HTTPCaller caller = new HTTPCaller(secure,host);
            _headers.put(Session.TARANTULA_TAG,"index/lobby");
            caller.doAction("user/index","onIndex",_headers,null,(json -> {
                isContinue(json);
                json.getAsJsonArray("lobbyList").forEach(lb->{
                    if(lb.getAsJsonObject().get("descriptor").getAsJsonObject().get("typeId").getAsString().equals(onGame.typeId())){
                        gameLobby = lb.getAsJsonObject().get("descriptor").getAsJsonObject();
                    }
                });
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
                        connection = json.get("connection").getAsJsonObject();
                    }
                });
            }
            if(continuing[0]){
                onWebSocket();
            }
            if(continuing[0]){
                //onGame and play
                onPlay(caller);
                //onProfile();
                waiting.countDown();
            }
            if(continuing[0]){
                waiting.await();
                offWebSocket();
                _headers.clear();
                _headers.put(Session.TARANTULA_TAG,"presence/lobby");
                _headers.put(Session.TARANTULA_TOKEN,presence.get("token").getAsString());
                caller.doAction("service/action","onAbsence",_headers,"{}".getBytes(),json -> {
                    JsonObject end = new JsonObject();
                    end.addProperty("message","onAbsence");
                    end.addProperty("successful",false);
                    isContinue(end);//end on logout
                });

            }
        }catch (Exception ex){
            JsonObject jex = new JsonObject();
            jex.addProperty("message",ex.getMessage());
            jex.addProperty("successful",false);
            isContinue(jex);
            ex.printStackTrace();
        }
        finally {
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
        webSocket.sendText(jo.toString(),true);
    }
    private void offWebSocket() throws Exception{
        JsonObject jo = new JsonObject();
        jo.addProperty("action","onStop");
        jo.addProperty("streaming",true);
        jo.addProperty("label","presence/notice");
        JsonObject jd = new JsonObject();
        jd.addProperty("command","onStop");
        jo.add("data",jd);
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
        dataBuffer.append(data);
        ws.request(1);
        if(last){
            onGame.onMessage(dataBuffer);
            dataBuffer.setLength(0);
            return null;
        }
        else{
            //System.out.println("more data coming ["+dataBuffer+"]");
            return this.accumulatedMessage;
        }
    }
    private void onPlay(HTTPCaller caller){
        _headers.put(Session.TARANTULA_TAG,gameLobby.get("tag").getAsString());
        _headers.put(Session.TARANTULA_TOKEN,presence.get("token").getAsString());
        JsonObject payload = new JsonObject();
        payload.addProperty("typeId",onGame.typeId());
        ConcurrentLinkedDeque<JsonObject> aQueue = new ConcurrentLinkedDeque<>();
        caller.doAction("service/action","onLobby",_headers,payload.toString().getBytes(),json->{
            json.get("gameList").getAsJsonArray().forEach(a->{
                aQueue.offer(a.getAsJsonObject());
            });
        });
        JsonObject game;
        do{
            game = aQueue.poll();
            if(game!=null){
                String appId = game.get("applicationId").getAsString();
                _headers.put(Session.TARANTULA_TAG,"presence/lobby");
                _headers.put(Session.TARANTULA_TOKEN,presence.get("token").getAsString());
                payload.addProperty("applicationId",appId);
                payload.addProperty("accessMode",Session.FAST_PLAY_MODE);
                caller.doAction("service/action","onPlay",_headers,payload.toString().getBytes(),jo->{
                    jo.addProperty("applicationId",appId);
                    onGame.onPlay(jo,webSocket,caller,presence);
                });
            }
        }while (game!=null);
    }
    private void onProfile(){
        JsonObject data = new JsonObject();
        data.addProperty("systemId",presence.get("systemId").getAsString());
        JsonObject payload = new JsonObject();
        payload.add("data",data);
        payload.addProperty("path","/service/action");
        payload.addProperty("tag","presence/profile");
        payload.addProperty("action","onProfile");
        payload.addProperty("streaming",false);
        webSocket.sendText(payload.toString(),true);
    }
}
