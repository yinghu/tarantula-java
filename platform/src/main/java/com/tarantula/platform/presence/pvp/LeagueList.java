package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;

import java.util.List;

public class LeagueList extends RecoverableObject {

    private final List<League> leagues;
    public LeagueList(List<League> leagues){
        this.leagues = leagues;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        JsonArray list = new JsonArray();
        leagues.forEach(  league-> list.add(league.toJson()));
        jsonObject.add("_leagues",list);
        return jsonObject;
    }
}
