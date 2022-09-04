package com.tarantula.platform.util;

import com.google.gson.*;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.platform.service.AppleStoreProvider;

import com.tarantula.platform.service.AuthVendorRegistry;
import com.tarantula.platform.service.ThirdPartyServiceProvider;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class AppleStoreCredentialsDeserializer implements JsonDeserializer<AuthVendorRegistry> {

    @Override
    public AuthVendorRegistry deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        //String certUri = jo.getAsJsonPrimitive("verifyUrl").getAsString();
        List<TokenValidatorProvider.AuthVendor> _validators = new ArrayList<>();
        jo.get("validators").getAsJsonArray().forEach((a)->{
            JsonObject sk = a.getAsJsonObject();
            _validators.add(new AppleStoreProvider(sk.get("name").getAsString(),sk.get("key").getAsString(),true));
        });
        return new ThirdPartyServiceProvider(OnAccess.APPLE_STORE,_validators);
    }
}
