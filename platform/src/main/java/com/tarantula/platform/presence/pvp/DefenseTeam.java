package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;

import java.util.ArrayList;
import java.util.List;

public class DefenseTeam extends RecoverableObject {

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

    public static DefenseTeam parse(byte[] payload){
        DefenseTeam defenseTeam = new DefenseTeam();
        JsonObject teamJson = JsonUtil.parse(payload);
        JsonArray unitInstancesJson = teamJson.get("unitInstances").getAsJsonArray();
        parseUnitInstance(unitInstancesJson,defenseTeam);
        JsonArray equipmentDataJson = arrayIfExists(teamJson,"equipmentData");
        parseEquipmentData(equipmentDataJson,defenseTeam);
        return defenseTeam;
    }

    private static void parseEquipmentData(JsonArray equipmentData,DefenseTeam defenseTeam){
        equipmentData.forEach(jsonElement -> {
            JsonObject jo = jsonElement.getAsJsonObject();
            EquipmentInstance equipmentInstance = new EquipmentInstance();
            JsonObject levelAndRank = jo.get("levelAndRank").getAsJsonObject();
            JsonObject primaryStat = jo.get("primaryStat").getAsJsonObject();
            JsonArray subStats = jo.get("subStats").getAsJsonArray();
            equipmentInstance.level = levelAndRank.get("level").getAsInt();
            equipmentInstance.rank = levelAndRank.get("rank").getAsInt();
            equipmentInstance.currentLevelExperience = levelAndRank.get("currentLevelExperience").getAsInt();
            equipmentInstance.currentRankExperience = levelAndRank.get("currentRankExperience").getAsInt();
            equipmentInstance.primaryStat.subStatRolledPercentageNormalized = primaryStat.get("subStatRolledPercentageNormalized").getAsFloat();
            equipmentInstance.primaryStat.hasSubStatRoll = primaryStat.get("hasSubStatRoll").getAsBoolean();
            equipmentInstance.primaryStat.statConfigID = primaryStat.get("statConfigID").getAsString();
            for(int i=0; i<EquipmentInstance.MAX_EQUIPMENT_STATS; i++){
                if(i<subStats.size()){
                    JsonObject stat = subStats.get(i).getAsJsonObject();
                    equipmentInstance.subStats[i].subStatRolledPercentageNormalized = stat.get("subStatRolledPercentageNormalized").getAsFloat();
                    equipmentInstance.subStats[i].hasSubStatRoll = stat.get("hasSubStatRoll").getAsBoolean();
                    equipmentInstance.subStats[i].statConfigID = stat.get("statConfigID").getAsString();
                }
            }
            defenseTeam.equipmentInstances.add(equipmentInstance);
        });
    }

    private static void parseUnitInstance(JsonArray unitInstancesJson,DefenseTeam defenseTeam){
        unitInstancesJson.forEach(jsonElement -> {
            JsonObject jo = jsonElement.getAsJsonObject();
            UnitInstance unitInstance = new UnitInstance();
            JsonObject levelAndRank = jo.get("levelAndRank").getAsJsonObject();
            JsonObject equipment = jo.get("equipment").getAsJsonObject();
            JsonObject abilityRanksWrapper = jo.get("abilityRanks").getAsJsonObject();
            unitInstance.level = levelAndRank.get("level").getAsInt();
            unitInstance.rank = levelAndRank.get("rank").getAsInt();
            unitInstance.currentLevelExperience = levelAndRank.get("currentLevelExperience").getAsInt();
            unitInstance.currentRankExperience = levelAndRank.get("currentRankExperience").getAsInt();
            unitInstance.weaponIDValue = longValueIfExists(equipment,"weaponIDValue");
            unitInstance.helmetIDValue = longValueIfExists(equipment,"helmetIDValue");
            unitInstance.chestPieceIDValue = longValueIfExists(equipment,"chestPieceIDValue");
            unitInstance.glovesIDValue = longValueIfExists(equipment,"glovesIDValue");
            unitInstance.forceFieldIDValue = longValueIfExists(equipment,"forceFieldIDValue");
            unitInstance.bootsIDValue = equipment.get("bootsIDValue").getAsLong();
            JsonArray abilityRanks = arrayIfExists(abilityRanksWrapper,"abilityRanks");
            for(int i=0; i<UnitInstance.MAX_ABILITY_RANKS; i++){
                unitInstance.abilityRanks[i]=0;
                if(i < abilityRanks.size()){
                    unitInstance.abilityRanks[i]=abilityRanks.get(i).getAsInt();
                }
            }
            unitInstance.passiveRank = abilityRanksWrapper.get("passiveRank").getAsInt();
            unitInstance.configID = jo.get("configID").getAsString();
            defenseTeam.unitInstances.add(unitInstance);
        });
    }

    private static long longValueIfExists(JsonObject jsonObject,String name){
        if(!jsonObject.has(name)) return 0;
        return jsonObject.get(name).getAsLong();
    }
    private static JsonArray arrayIfExists(JsonObject jsonObject,String name){
        if(!jsonObject.has(name)) return new JsonArray();
        return jsonObject.get(name).getAsJsonArray();
    }


}
