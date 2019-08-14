package com.tarantula.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.game.CommandResponse;

import java.lang.reflect.Type;

/**
 * Updated by yinghu lu on 4/29/2019
 */
public class CommandResponseSerializer implements JsonSerializer<CommandResponse> {


    public JsonElement serialize(CommandResponse resp, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = (JsonObject)new GameComponentSerializer().serialize(resp,type,jsonSerializationContext);
        jo.addProperty("command",resp.command());
        jo.addProperty("message",resp.message());
        jo.addProperty("code",resp.code());
        jo.addProperty("successful",resp.successful());
        jo.addProperty("index",resp.subscript);
        jo.addProperty("balance",resp.balance());
        return jo;
    }
}
