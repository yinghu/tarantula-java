package com.tarantula.platform.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.icodesoftware.Account;

import java.lang.reflect.Type;

public class AccountSerializer implements JsonSerializer<Account> {

    public JsonElement serialize(Account access, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        jo.addProperty("trial",access.trial());
        jo.addProperty("subscribed",access.subscribed());
        jo.addProperty("userCount",access.userCount(0));
        jo.addProperty("gameClusterCount",access.gameClusterCount(0));
        jo.addProperty("revision",Long.toString(access.revision()));
        return jo;
    }
}
