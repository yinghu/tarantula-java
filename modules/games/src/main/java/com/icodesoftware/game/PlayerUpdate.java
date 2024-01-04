package com.icodesoftware.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.protocol.Channel;
import com.icodesoftware.util.RecoverableObject;

import java.util.concurrent.ConcurrentHashMap;


public class PlayerUpdate extends RecoverableObject {

    public String systemId;
    public int stub;
    public GameExperience[] gameExperiences;

    public Channel channel;

    private ConcurrentHashMap<String,GameExperience> pendingUpdates;
    public PlayerUpdate(String systemId,int stub, GameExperience[] gameExperiences){
        this.systemId = systemId;
        this.stub = stub;
        this.gameExperiences = gameExperiences;
    }
    public PlayerUpdate(Channel channel){
        this.systemId = channel.owner();
        this.stub = channel.routingNumber();
        this.channel = channel;
        this.pendingUpdates = new ConcurrentHashMap<>();
    }

    public void update(String name,double statisticsDelta,double experienceDelta){
        pendingUpdates.compute(name,(k,e)->{
            if(e==null) e = new GameExperience(name,0,0);
            e.statisticsDelta += statisticsDelta;
            e.experienceDelta += experienceDelta;
            return e;
        });
    }

    public void toBatch(){
        gameExperiences = new GameExperience[pendingUpdates.size()];
        int[] i={0};
        pendingUpdates.forEach((k,v)->{
            gameExperiences[i[0]++]=v;
        });
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("systemId",systemId);
        jsonObject.addProperty("stub",stub);
        JsonArray updates = new JsonArray();
        for(GameExperience gameExperience : gameExperiences){
            updates.add(gameExperience.toJson());
        }
        jsonObject.add("updates",updates);
        return jsonObject;
    }

    public static PlayerUpdate fromJson(JsonObject payload){
        String systemId = payload.get("systemId").getAsString();
        int stub = payload.get("stub").getAsInt();
        JsonArray updates = payload.getAsJsonArray("updates");
        GameExperience[] gameExperiences = new GameExperience[updates.size()];
        int index = 0;
        for(JsonElement update : payload.getAsJsonArray("updates")){
            gameExperiences[index++]=GameExperience.fromJson(update.getAsJsonObject());
        }
        return new PlayerUpdate(systemId,stub,gameExperiences);
    }
}
