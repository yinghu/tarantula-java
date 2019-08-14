package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.TokenValidator;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 1/31/2019.
 */
public class OAuthVendorSerializer implements JsonSerializer<TokenValidator.OAuthVendor> {

    @Override
    public JsonElement serialize(TokenValidator.OAuthVendor oAuthObject, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("name",oAuthObject.name());
        jo.addProperty("clientId",oAuthObject.clientId());
        return jo;
    }
}
