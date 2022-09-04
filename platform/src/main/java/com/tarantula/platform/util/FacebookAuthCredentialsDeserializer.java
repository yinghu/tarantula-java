package com.tarantula.platform.util;

import com.google.gson.*;
import com.icodesoftware.OnAccess;
import com.icodesoftware.service.TokenValidatorProvider;
import com.tarantula.platform.service.AuthVendorRegistry;
import com.tarantula.platform.service.FacebookAuthProvider;
import com.tarantula.platform.service.ThirdPartyServiceProvider;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class FacebookAuthCredentialsDeserializer implements JsonDeserializer<AuthVendorRegistry> {

    @Override
    public AuthVendorRegistry deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        List<TokenValidatorProvider.AuthVendor> _validators = new ArrayList<>();
        JsonArray jv = jsonElement.getAsJsonObject().get("validators").getAsJsonArray();
        jv.forEach(e-> {
            JsonObject jo = e.getAsJsonObject();
            String typeId = jo.getAsJsonPrimitive("typeId").getAsString();
            String appName = jo.getAsJsonPrimitive("appName").getAsString();
            String appId = jo.getAsJsonPrimitive("appId").getAsString();
            String secureKey = jo.getAsJsonPrimitive("secureKey").getAsString();
            //String certUri = jo.getAsJsonPrimitive("certUrl").getAsString();
            //String authUri = jo.getAsJsonPrimitive("authUrl").getAsString();
            //String tokenUri = jo.getAsJsonPrimitive("tokenUrl").getAsString();
            _validators.add(new FacebookAuthProvider(typeId,appId,secureKey));
        });
        return new ThirdPartyServiceProvider(OnAccess.FACEBOOK,_validators);
    }
}
