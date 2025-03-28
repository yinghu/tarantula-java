package com.tarantula.platform.presence.pvp;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class DefenseFormationSavedAnalytic extends PVPAnalytic{

    private static final String MESSAGE_TYPE = "/earth8/pvp/0.0.1/defenseFormationSaved";

    public DefenseFormationSavedAnalytic(BattleTeam defenseTeam)
    {
        super(MESSAGE_TYPE);
        data.addProperty("player_id", defenseTeam.playerId);
        data.addProperty("team_power", defenseTeam.teamPower);

        JsonArray units = new JsonArray();
        defenseTeam.unitInstances.forEach(unitInstance -> units.add(unitInstance.toJson()));
        JsonArray equips = new JsonArray();
        defenseTeam.equipmentInstances.forEach(equipmentInstance -> equips.add(equipmentInstance.toJson()));

        data.add("unit_instances", units);
        data.add("equipment_instances", units);
    }
}
