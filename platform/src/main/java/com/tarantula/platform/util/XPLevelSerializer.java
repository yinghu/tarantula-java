package com.tarantula.platform.util;

import com.google.gson.*;
import com.tarantula.platform.leveling.XPLevel;


import java.lang.reflect.Type;

/**
 * Updated by yinghu on 4/23/2018.
 */
public class XPLevelSerializer implements JsonSerializer<XPLevel> {

    public JsonElement serialize(XPLevel level, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jb = new JsonObject();
        jb.addProperty("level",level.level());
        jb.addProperty("name",level.levelView.name);
        jb.addProperty("icon",level.levelView.icon);
        jb.addProperty("levelXP",level.levelXP(0));
        return jb;
    }
}
