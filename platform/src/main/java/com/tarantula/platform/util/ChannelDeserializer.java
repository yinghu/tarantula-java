package com.tarantula.platform.util;

import com.google.gson.*;
import com.icodesoftware.Channel;
import com.icodesoftware.Connection;
import com.tarantula.cci.udp.GameChannel;
import com.tarantula.cci.udp.UDPChannel;
import com.tarantula.platform.UniverseConnection;

import java.lang.reflect.Type;

public class ChannelDeserializer implements JsonDeserializer<Channel> {

    @Override
    public Channel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        Channel desc = toChannel(jo);
        return desc;
    }
    private Channel toChannel(JsonObject jo){
        return new GameChannel(jo.get("channelId").getAsInt(),jo.get("sessionId").getAsInt());
    }
}
