package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.OnPlay;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Updated by yinghu on 4/24/2018.
 */
public class OnPlaySerializer implements JsonSerializer<OnPlay> {

    public JsonElement serialize(OnPlay onPlay, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jb = new JsonObject();
        jb.addProperty("systemId",onPlay.systemId());
        jb.addProperty("applicationId",onPlay.applicationId());
        jb.addProperty("instanceId",onPlay.instanceId());
        jb.addProperty("name",onPlay.name());
        jb.addProperty("playTime", LocalDateTime.ofInstant(Instant.ofEpochMilli(onPlay.timestamp()), ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        jb.addProperty("balance",onPlay.balance());
        return jb;
    }
}
