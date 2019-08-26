package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.Profile;

import java.lang.reflect.Type;


/**
 * Updated by yinghu on 8/26/19
 */
public class ProfileSerializer implements JsonSerializer<Profile> {
    @Override
    public JsonElement serialize(Profile profile, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("nickname",profile.nickname());
        jo.addProperty("avatar",profile.avatar());
        return jo;
    }
}
