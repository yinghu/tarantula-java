package com.tarantula.platform.util;

import com.google.gson.*;

import com.tarantula.platform.leaderboard.LeaderBoardRegistry;
import com.tarantula.platform.leaderboard.LeaderBoardRegistryContext;

import java.lang.reflect.Type;

/**
 * Created by yinghu lu on 5/14/2018.
 */
public class LeaderBoardRegistryContextSerializer implements JsonSerializer<LeaderBoardRegistryContext> {
    @Override
    public JsonElement serialize(LeaderBoardRegistryContext leaderBoardRegistryContext, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject pc = (JsonObject)new ResponseSerializer().serialize(leaderBoardRegistryContext,type,jsonSerializationContext);
        LeaderBoardRegistrySerializer serializer = new LeaderBoardRegistrySerializer();
        JsonArray blist = new JsonArray();
        //leaderBoardRegistryContext.registryList.forEach((r)->{
            //blist.add(serializer.serialize((LeaderBoardRegistry) r,type,jsonSerializationContext));
        //});
        pc.add("registryList",blist);
        return pc;
    }
}
