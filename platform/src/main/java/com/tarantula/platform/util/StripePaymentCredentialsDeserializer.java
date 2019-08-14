package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.platform.GoogleOAuthProvider;
import com.tarantula.platform.OAuthObject;
import com.tarantula.platform.StripePaymentProvider;

import java.lang.reflect.Type;


/**
 * Created by yinghu lu on 1/31/2019.
 */
public class StripePaymentCredentialsDeserializer implements JsonDeserializer<OAuthObject> {

    @Override
    public OAuthObject deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        String clientId = jo.getAsJsonPrimitive("public_key").getAsString();
        String secureKey = jo.getAsJsonPrimitive("private_key").getAsString();
        //String authUri = jo.getAsJsonPrimitive("auth_uri").getAsString();
        //String tokenUri = jo.getAsJsonPrimitive("token_uri").getAsString();
        //String certUri = jo.getAsJsonPrimitive("auth_provider_x509_cert_url").getAsString();
        return new StripePaymentProvider(clientId,secureKey);
    }
}
