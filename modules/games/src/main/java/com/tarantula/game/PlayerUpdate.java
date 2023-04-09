package com.tarantula.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.JsonSerializable;


public class PlayerUpdate implements JsonSerializable {

    public String systemId;
    public GameExperience[] gameExperiences;
    public PlayerUpdate(String systemId, GameExperience[] gameExperiences){
        this.systemId = systemId;
        this.gameExperiences = gameExperiences;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("systemId",systemId);
        JsonArray updates = new JsonArray();
        for(GameExperience gameExperience : gameExperiences){
            updates.add(gameExperience.toJson());
        }
        jsonObject.add("updates",updates);
        return jsonObject;
    }

    public static PlayerUpdate fromJson(JsonObject payload){
        String systemId = payload.get("systemId").getAsString();
        JsonArray updates = payload.getAsJsonArray("updates");
        GameExperience[] gameExperiences = new GameExperience[updates.size()];
        int index = 0;
        for(JsonElement update : payload.getAsJsonArray("updates")){
            gameExperiences[index++]=GameExperience.fromJson(update.getAsJsonObject());
        }
        return new PlayerUpdate(systemId,gameExperiences);
    }
}
