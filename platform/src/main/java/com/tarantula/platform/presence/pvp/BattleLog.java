package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;

public class BattleLog extends RecoverableObject {

    public int offenseEloGain; //positive for win , nagitive for lost
    public int defenseEloGain;
    public int defenseElo;

    public BattleTeam defenseTeam;
    public BattleTeam offenseTeam;

    public BattleLog(BattleLogIndex battleLogIndex){
        this.offenseEloGain = battleLogIndex.offenseEloGain;
        this.defenseEloGain = battleLogIndex.defenseEloGain;
        this.defenseElo = battleLogIndex.defenseElo;
    }


    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("OffenseWin",offenseEloGain>0);
        jsonObject.addProperty("DefenseWin",defenseEloGain>0);
        jsonObject.addProperty("OffenseEloGain",offenseEloGain);
        jsonObject.addProperty("DefenseEloGain",defenseEloGain);
        jsonObject.addProperty("DefenseElo",defenseElo);
        jsonObject.add("_defenseTeam",defenseTeam.toJson());
        jsonObject.add("_offenseTeam",offenseTeam.toJson());
        return jsonObject;
    }
}
