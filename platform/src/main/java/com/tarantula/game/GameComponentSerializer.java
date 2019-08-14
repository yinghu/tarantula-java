package com.tarantula.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Updated by yinghu lu on 5/5/2019.
 */
public class GameComponentSerializer implements JsonSerializer<GameComponent> {
    @Override
    public JsonElement serialize(GameComponent gameComponent, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("systemId",gameComponent.systemId());
        jo.addProperty("componentId",gameComponent.componentId!=null?gameComponent.componentId:gameComponent.name());
        jo.addProperty("index",gameComponent.subscript);
        jo.addProperty("name",gameComponent.name());
        jo.addProperty("label",gameComponent.label());
        return jo;
    }
}
