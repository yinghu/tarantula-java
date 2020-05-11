package com.tarantula.platform.util;

import com.google.gson.*;

import com.tarantula.platform.service.AuthObject;
import com.tarantula.platform.service.GoogleOAuthProvider;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 5/11/2020
 */
public class GoogleAuthCredentialsDeserializer implements JsonDeserializer<AuthObject> {

    @Override
    public AuthObject deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject().get("web").getAsJsonObject();
        String clientId = jo.getAsJsonPrimitive("client_id").getAsString();
        String secureKey = jo.getAsJsonPrimitive("client_secret").getAsString();
        String authUri = jo.getAsJsonPrimitive("auth_uri").getAsString();
        String tokenUri = jo.getAsJsonPrimitive("token_uri").getAsString();
        String certUri = jo.getAsJsonPrimitive("auth_provider_x509_cert_url").getAsString();
        return new GoogleOAuthProvider(clientId,secureKey,authUri,tokenUri,certUri,new String[0]);
    }
}
