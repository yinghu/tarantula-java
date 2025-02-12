package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonObject;
import com.icodesoftware.JsonSerializable;

import java.util.List;
import java.util.Map;

public class BattleLogList implements JsonSerializable {

    private List<BattleLog> battleLogs;
    public BattleLogList(List<BattleLog> battleLogs){
        this.battleLogs = battleLogs;
    }

    @Override
    public Map<String, Object> toMap() {
        return Map.of();
    }

    @Override
    public void fromMap(Map<String, Object> properties) {

    }

    @Override
    public JsonObject toJson() {
        return null;
    }
}
