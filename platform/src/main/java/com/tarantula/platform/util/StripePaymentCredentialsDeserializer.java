package com.tarantula.platform.util;

import com.google.gson.*;

import com.tarantula.platform.service.AuthObject;
import com.tarantula.platform.service.StripePaymentProvider;

import java.lang.reflect.Type;


/**
 * Created by yinghu lu on 5/14/2020
 */
public class StripePaymentCredentialsDeserializer implements JsonDeserializer<AuthObject> {

    @Override
    public AuthObject deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        String clientId = jo.getAsJsonPrimitive("public_key").getAsString();
        String secureKey = jo.getAsJsonPrimitive("private_key").getAsString();
        return new StripePaymentProvider(clientId,secureKey);
    }
}
