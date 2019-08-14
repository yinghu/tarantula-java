package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.platform.presence.ProfileContext;

import java.lang.reflect.Type;

/**
 * Updated by yinghu on 10/7/2018.
 */
public class ProfileContextSerializer implements JsonSerializer<ProfileContext> {
    @Override
    public JsonElement serialize(ProfileContext profileContext, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject pc = (JsonObject)new ResponseSerializer().serialize(profileContext,type,jsonSerializationContext);
        if(profileContext.profile!=null){
            pc.add("profile",new ProfileSerializer().serialize(profileContext.profile,type,jsonSerializationContext));
        }
        return pc;
    }
}
