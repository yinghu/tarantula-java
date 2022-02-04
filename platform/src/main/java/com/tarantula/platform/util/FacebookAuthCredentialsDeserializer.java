package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.platform.service.AuthObject;
import com.tarantula.platform.service.FacebookAuthProvider;

import java.lang.reflect.Type;


public class FacebookAuthCredentialsDeserializer implements JsonDeserializer<AuthObject> {

    @Override
    public AuthObject deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        String appName = jo.getAsJsonPrimitive("appName").getAsString();
        String appId = jo.getAsJsonPrimitive("appId").getAsString();
        String accessToken = jo.getAsJsonPrimitive("accessToken").getAsString();
        String authUri = jo.getAsJsonPrimitive("authUrl").getAsString();
        String tokenUri = jo.getAsJsonPrimitive("tokenUrl").getAsString();
        return new FacebookAuthProvider(appId,accessToken,authUri,tokenUri,"",new String[]{appName});
    }
}
