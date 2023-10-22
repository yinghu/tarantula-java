package com.icodesoftware.game;

import com.google.gson.JsonObject;
import com.icodesoftware.JsonSerializable;
import com.icodesoftware.util.RecoverableObject;

public class GameExperience extends RecoverableObject {

    public String name;
    public double statisticsDelta;
    public double experienceDelta;

    public GameExperience(String name,double statisticsDelta,double experienceDelta){
        this.name = name;
        this.statisticsDelta = statisticsDelta;
        this.experienceDelta = experienceDelta;
    }
    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",name);
        jsonObject.addProperty("statisticsDelta",statisticsDelta);
        jsonObject.addProperty("experienceDelta",experienceDelta);
        return jsonObject;
    }

    public static GameExperience fromJson(JsonObject payload){
        return new GameExperience(payload.get("name").getAsString(),payload.get("statisticsDelta").getAsDouble(),payload.get("experienceDelta").getAsDouble());
    }
}
