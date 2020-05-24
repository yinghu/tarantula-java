package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.Account;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

/**
 * Created by yinghu lu on 5/13/2020
 */
public class AccountSerializer implements JsonSerializer<Account> {

    public JsonElement serialize(Account access, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("trial",access.trial());
        jo.addProperty("subscribed",access.subscribed());
        jo.addProperty("emailAddress",access.emailAddress());
        jo.addProperty("userCount",access.userCount(0));
        jo.addProperty("gameClusterCount",access.gameClusterCount(0));
        jo.addProperty("lastUpdated",SystemUtil.fromUTCMilliseconds(access.timestamp()).format(DateTimeFormatter.ISO_DATE_TIME));
        return jo;
    }
}
