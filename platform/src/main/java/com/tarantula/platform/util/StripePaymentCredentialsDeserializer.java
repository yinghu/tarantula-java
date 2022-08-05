package com.tarantula.platform.util;

import com.google.gson.*;

import com.tarantula.platform.service.AuthVendorRegistry;
import com.tarantula.platform.service.StripePaymentProvider;

import java.lang.reflect.Type;


public class StripePaymentCredentialsDeserializer implements JsonDeserializer<AuthVendorRegistry> {

    @Override
    public AuthVendorRegistry deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        String clientId = jo.getAsJsonPrimitive("public_key").getAsString();
        String secureKey = jo.getAsJsonPrimitive("private_key").getAsString();
        return new StripePaymentProvider(clientId,secureKey);
    }
}
