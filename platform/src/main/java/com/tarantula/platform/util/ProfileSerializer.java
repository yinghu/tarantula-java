package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.Profile;

import java.lang.reflect.Type;


/**
 * Updated by yinghu on 10/7/2018.
 */
public class ProfileSerializer implements JsonSerializer<Profile> {
    @Override
    public JsonElement serialize(Profile profile, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("nickname",profile.nickname());
        jo.addProperty("avatar",profile.avatar());
        jo.addProperty("video",profile.video());
        return jo;
    }
}
