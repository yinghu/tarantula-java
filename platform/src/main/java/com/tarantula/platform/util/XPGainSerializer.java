package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.OnLeaderBoard;
import com.tarantula.XP;

import java.lang.reflect.Type;

/**
 * Updated by yinghu lu on 4/23/2018
 */
public class XPGainSerializer implements JsonSerializer<XP> {
    @Override
    public JsonElement serialize(XP xpGain, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jb = new JsonObject();
        jb.addProperty("header",xpGain.header());
        jb.addProperty("name",xpGain.category());
        JsonArray xlist = new JsonArray();
        JsonObject kv = new JsonObject();
        kv.addProperty("category", OnLeaderBoard.DAILY);
        kv.addProperty("value",xpGain.dailyGain(0));
        xlist.add(kv);
        JsonObject kv1 = new JsonObject();
        kv1.addProperty("category", OnLeaderBoard.WEEKLY);
        kv1.addProperty("value",xpGain.weeklyGain(0));
        xlist.add(kv1);
        JsonObject kv2 = new JsonObject();
        kv2.addProperty("category", OnLeaderBoard.MONTHLY);
        kv2.addProperty("value",xpGain.monthlyGain(0));
        xlist.add(kv2);
        JsonObject kv3 = new JsonObject();
        kv3.addProperty("category", OnLeaderBoard.YEARLY);
        kv3.addProperty("value",xpGain.yearlyGain(0));
        xlist.add(kv3);
        JsonObject kv4 = new JsonObject();
        kv4.addProperty("category", OnLeaderBoard.TOTAL);
        kv4.addProperty("value",xpGain.totalGain(0));
        xlist.add(kv4);
        jb.add("statistics",xlist);
        return jb;
    }
}
