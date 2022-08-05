package com.tarantula.platform.util;

import com.google.gson.*;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.platform.service.AuthVendorRegistry;
import com.tarantula.platform.service.MockStoreProvider;
import com.tarantula.platform.service.ThirdPartyServiceProvider;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MockStoreCredentialsDeserializer implements JsonDeserializer<AuthVendorRegistry> {

    @Override
    public AuthVendorRegistry deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        List<TokenValidatorProvider.AuthVendor> authList = new ArrayList<>();
        jo.get("serviceKeys").getAsJsonArray().forEach((a)->{
            JsonObject sk = a.getAsJsonObject();
            authList.add(new MockStoreProvider(sk.get("name").getAsString(),sk.get("key").getAsString()));
        });
        return new ThirdPartyServiceProvider(OnAccess.MOCK_STORE,authList);
    }
}
