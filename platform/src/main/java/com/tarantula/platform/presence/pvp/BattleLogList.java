package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;

import java.util.List;

public class BattleLogList extends RecoverableObject {

    private final List<BattleLog> battleLogs;
    public BattleLogList(List<BattleLog> battleLogs){
        this.battleLogs = battleLogs;
    }



    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        JsonArray logs = new JsonArray();
        battleLogs.forEach(battleLog -> logs.add(battleLog.toJson()));
        jsonObject.add("_battleLogs",logs);
        return jsonObject;
    }
}
