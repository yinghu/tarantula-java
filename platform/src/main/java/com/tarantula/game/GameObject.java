package com.tarantula.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.tarantula.platform.OnApplicationHeader;


import java.lang.reflect.Type;

public class GameObject extends OnApplicationHeader {

    public JsonElement setup(Type type, JsonSerializationContext jsonSerializationContext){
        return new JsonObject();
    }
}
