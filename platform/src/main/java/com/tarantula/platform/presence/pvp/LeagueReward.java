package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonObject;
import com.tarantula.platform.item.Application;


public class LeagueReward extends Application {

    public LeagueReward(JsonObject config){
        this.application = config;
    }

    @Override
    public long distributionId(){
        return Long.parseLong(this.application.get("ItemId").getAsString());
    }

    @Override
    public JsonObject toJson() {
        return application;
    }
}
