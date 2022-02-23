package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.platform.service.AppleStoreProvider;
import com.tarantula.platform.service.AuthObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


public class AppleStoreCredentialsDeserializer implements JsonDeserializer<AuthObject> {

    @Override
    public AuthObject deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        String certUri = jo.getAsJsonPrimitive("verifyUrl").getAsString();
        Map<String,String> serviceKeys = new HashMap<>();
        jo.get("serviceKeys").getAsJsonArray().forEach((a)->{
            JsonObject sk = a.getAsJsonObject();
            serviceKeys.put(sk.get("name").getAsString(),sk.get("key").getAsString());
        });
        return new AppleStoreProvider(certUri,serviceKeys);
    }
}
