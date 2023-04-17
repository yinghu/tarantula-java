package com.icodesoftware.game;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.JsonSerializable;
import com.icodesoftware.util.JsonUtil;

public class UpdateBatch implements JsonSerializable {

    public PlayerUpdate[] playerUpdates;

    public UpdateBatch(PlayerUpdate[] playerUpdates){
        this.playerUpdates = playerUpdates;
    }
    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        JsonArray batch = new JsonArray();
        for(PlayerUpdate playerUpdate : playerUpdates){
            batch.add(playerUpdate.toJson());
        }
        jsonObject.add("batch",batch);
        return jsonObject;
    }

    public byte[] toBytes(){
        return toJson().toString().getBytes();
    }

    public static UpdateBatch fromBytes(byte[] payload){
        JsonObject jsonObject = JsonUtil.parse(payload);
        JsonArray batch = jsonObject.getAsJsonArray("batch");
        PlayerUpdate[] playerUpdates = new PlayerUpdate[batch.size()];
        int index = 0;
        for(JsonElement update : batch){
            playerUpdates[index++]=PlayerUpdate.fromJson(update.getAsJsonObject());
        }
        return new UpdateBatch(playerUpdates);
    }
}
