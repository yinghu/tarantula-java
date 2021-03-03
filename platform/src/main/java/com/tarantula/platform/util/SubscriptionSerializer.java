package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.icodesoftware.Subscription;
import com.icodesoftware.util.TimeUtil;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

/**
 * Created by yinghu lu on 5/13/2020
 */
public class SubscriptionSerializer implements JsonSerializer<Subscription> {

    public JsonElement serialize(Subscription access, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("startTime", TimeUtil.fromUTCMilliseconds(access.startTimestamp()).format(DateTimeFormatter.ISO_DATE_TIME));
        jo.addProperty("endTime",TimeUtil.fromUTCMilliseconds(access.endTimestamp()).format(DateTimeFormatter.ISO_DATE_TIME));
        jo.addProperty("lastUpdated",TimeUtil.fromUTCMilliseconds(access.timestamp()).format(DateTimeFormatter.ISO_DATE_TIME));
        jo.addProperty("updatedCount",access.count(0));
        return jo;
    }
}
