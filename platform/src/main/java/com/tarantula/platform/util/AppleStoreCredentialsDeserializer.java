package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.platform.service.AuthObject;
import com.tarantula.platform.service.FacebookAuthProvider;

import java.lang.reflect.Type;


public class AppleStoreCredentialsDeserializer implements JsonDeserializer<AuthObject> {

    @Override
    public AuthObject deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        String secureKey = jo.getAsJsonPrimitive("secureKey").getAsString();
        String certUri = jo.getAsJsonPrimitive("verifyUrl").getAsString();
        return new FacebookAuthProvider("",secureKey,"","",certUri,new String[0]);
    }
}
