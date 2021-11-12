package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.platform.room.ChannelStub;

import java.lang.reflect.Type;

public class ChannelDeserializer implements JsonDeserializer<ChannelStub> {

    @Override
    public ChannelStub deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        return new ChannelStub(jo.get("channelId").getAsInt(),jo.get("timeout").getAsInt());
    }
}
