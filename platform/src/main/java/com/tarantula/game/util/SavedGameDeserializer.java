package com.tarantula.game.util;

import com.google.gson.*;
import com.tarantula.platform.presence.saves.SavedGame;

import java.lang.reflect.Type;


public class SavedGameDeserializer implements JsonDeserializer<SavedGame> {

    @Override
    public SavedGame deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        SavedGame savedGame = new SavedGame();
        savedGame.name(jsonObject.get("DeviceName").getAsString());
        savedGame.index(jsonObject.get("DeviceId").getAsString());
        savedGame.owner(jsonObject.get("OwnerId").getAsString());
        savedGame.version = (jsonObject.get("Version").getAsInt());
        savedGame.timestamp(jsonObject.get("Timestamp").getAsLong());
        savedGame.distributionKey(jsonObject.get("GameId").getAsString());
        return savedGame;
    }
}
