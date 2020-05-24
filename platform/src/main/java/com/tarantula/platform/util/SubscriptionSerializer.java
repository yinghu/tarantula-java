package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.Account;
import com.tarantula.Subscription;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

/**
 * Created by yinghu lu on 5/13/2020
 */
public class SubscriptionSerializer implements JsonSerializer<Subscription> {

    public JsonElement serialize(Subscription access, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("trial",access.trial());
        jo.addProperty("subscribed",access.subscribed());
        jo.addProperty("startTime",SystemUtil.fromUTCMilliseconds(access.startTimestamp()).format(DateTimeFormatter.ISO_DATE_TIME));
        jo.addProperty("endTime",SystemUtil.fromUTCMilliseconds(access.endTimestamp()).format(DateTimeFormatter.ISO_DATE_TIME));
        jo.addProperty("lastUpdated",SystemUtil.fromUTCMilliseconds(access.timestamp()).format(DateTimeFormatter.ISO_DATE_TIME));
        return jo;
    }
}
