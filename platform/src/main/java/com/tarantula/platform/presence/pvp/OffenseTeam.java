package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;

import java.util.ArrayList;
import java.util.List;

public class OffenseTeam extends RecoverableObject {

    public final List<UnitInstance> unitInstances = new ArrayList<>();
    public final List<EquipmentInstance> equipmentInstances = new ArrayList<>();

    @Override
    public JsonObject toJson() {
        JsonObject resp = new JsonObject();
        JsonArray units = new JsonArray();
        unitInstances.forEach(unitInstance -> units.add(unitInstance.toJson()));
        JsonArray equips = new JsonArray();
        equipmentInstances.forEach(equipmentInstance -> equips.add(equipmentInstance.toJson()));
        resp.add("unitInstances",units);
        resp.add("equipmentData",equips);
        return resp;
    }

    public static JsonObject levelAndRank(int level,int rank,int levelExp,int rankExp){
        JsonObject levelAndRank = new JsonObject();
        levelAndRank.addProperty("level",level);
        levelAndRank.addProperty("rank",rank);
        levelAndRank.addProperty("currentLevelExperience",levelExp);
        levelAndRank.addProperty("currentRankExperience",rankExp);
        return levelAndRank;
    }
    public static JsonObject abilityRanks(int[] ranks,int passiveRank){
        JsonObject abilityRanks = new JsonObject();
        return abilityRanks;
    }
    public static JsonObject equipment(long weaponIDValue,long helmetIDValue,long chestPieceIDValue,long glovesIDValue,long forceFieldIDValue,long bootsIDValue){
        JsonObject equipment = new JsonObject();
        equipment.addProperty("weaponIDValue",weaponIDValue);
        equipment.addProperty("helmetIDValue",helmetIDValue);
        equipment.addProperty("chestPieceIDValue",chestPieceIDValue);
        equipment.addProperty("glovesIDValue",glovesIDValue);
        equipment.addProperty("forceFieldIDValue",forceFieldIDValue);
        equipment.addProperty("bootsIDValue",bootsIDValue);
        return equipment;
    }

    public static JsonArray subStats(EquipmentStat[] equipmentStats){
        JsonArray subStats = new JsonArray();
        for(EquipmentStat stat : equipmentStats){
            if(stat==null) continue;
            subStats.add(stat.toJson());
        }
        return subStats;
    }

}
