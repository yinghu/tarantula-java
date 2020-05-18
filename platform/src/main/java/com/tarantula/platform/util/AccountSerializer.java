package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tarantula.Account;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 5/13/2020
 */
public class AccountSerializer implements JsonSerializer<Account> {

    public JsonElement serialize(Account access, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("emailAddress",access.emailAddress());
        jo.addProperty("userCount",access.userCount());
        jo.addProperty("gameClusterCount",access.gameClusterCount());
        jo.addProperty("trial",access.trial());
        return jo;
    }
}
