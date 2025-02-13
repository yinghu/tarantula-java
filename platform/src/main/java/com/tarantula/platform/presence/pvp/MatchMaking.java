package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.JsonSerializable;

import java.util.List;
import java.util.Map;

public class MatchMaking implements JsonSerializable {

    public long nextRefreshTime;
    public List<BattleTeam> battleTeams;

    @Override
    public Map<String, Object> toMap() {
        return Map.of();
    }

    @Override
    public void fromMap(Map<String, Object> properties) {

    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("NextRefreshTime",nextRefreshTime);
        JsonArray teams = new JsonArray();
        battleTeams.forEach(battleTeam -> teams.add(battleTeam.toJson()));
        jsonObject.add("_opponents",teams);
        return jsonObject;
    }
}
