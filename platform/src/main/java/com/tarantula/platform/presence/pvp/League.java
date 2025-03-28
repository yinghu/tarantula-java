package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonObject;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.resource.GameResource;

public class League extends GameResource {

    public PostBattleReward postBattleReward;
    public PlacementReward placementReward;
    public LeagueReward leagueReward;

    public League(ConfigurableObject configurableObject){
        super(configurableObject);
    }

    public int startPoint(){
        return header.get("StartPoint").getAsInt();
    }

    public int endPoint(){
        return header.get("EndPoint").getAsInt();
    }

    public JsonObject toJson(){
        return super.toJson();
    }
}
