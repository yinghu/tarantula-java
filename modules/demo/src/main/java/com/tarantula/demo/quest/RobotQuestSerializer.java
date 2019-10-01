package com.tarantula.demo.quest;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by yinghu 9/30/2019
 */
public class RobotQuestSerializer implements JsonSerializer<RobotQuest> {
    @Override
    public JsonElement serialize(RobotQuest rq, Type type, JsonSerializationContext jsonSerializationContext) {
        return rq.toJson();
    }
}
