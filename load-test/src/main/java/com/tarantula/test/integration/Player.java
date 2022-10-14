package com.tarantula.test.integration;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.tarantula.test.HTTPCaller;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;

public class Player implements Runnable{

    private boolean secure;
    private String host;
    private CountDownLatch counter;
    private HashMap<String,String> _headers = new HashMap<>();
    private String userName;
    private OnPayload done;
    private boolean[] continuing = {false};
    private JsonObject presence;
    private JsonObject connection;

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
                //onGame and play
                onPlay(caller);
                //onProfile();
                waiting.countDown();
            }
            if(continuing[0]){
                waiting.await();
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
                });
            }
        }while (game!=null);
    }
}
