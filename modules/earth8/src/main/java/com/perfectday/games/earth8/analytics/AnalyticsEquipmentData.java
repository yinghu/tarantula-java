package com.perfectday.games.earth8.analytics;

import com.google.gson.JsonObject;
import com.perfectday.games.earth8.BattleUpdate;

public class AnalyticsEquipmentData {
    public String EquipmentName;
    public String EquipmentSet;
    public String EquipmentType;
    public String EquipmentPrimaryStat;
    public String EquipmentRarity;
    public int EquipmentLevel;
    public int EquipmentRank;

    public AnalyticsEquipmentData(JsonObject data)
    {
        EquipmentName = BattleUpdate.GetJsonString(data, "TEMP_EquipmentName", "");
        EquipmentSet = BattleUpdate.GetJsonString(data, "TEMP_EquipmentSet", "");
        EquipmentType = BattleUpdate.GetJsonString(data, "TEMP_EquipmentType", "");
        EquipmentPrimaryStat = BattleUpdate.GetJsonString(data, "TEMP_EquipmentPrimaryStat", "");
        EquipmentRarity = BattleUpdate.GetJsonString(data, "TEMP_EquipmentRarity", "");
        EquipmentLevel = BattleUpdate.GetJsonInt(data, "TEMP_EquipmentLevel", 0);
        EquipmentRank = BattleUpdate.GetJsonInt(data, "TEMP_EquipmentRank", 0);
    }

    public void addToObject(JsonObject data)
    {
        data.addProperty("equipment_name", EquipmentName);
        data.addProperty("equipment_set", EquipmentSet);
        data.addProperty("equipment_type", EquipmentType);
        data.addProperty("equipment_primary_stat", EquipmentPrimaryStat);
        data.addProperty("equipment_rarity", EquipmentRarity);
        data.addProperty("equipment_level", EquipmentLevel);
        data.addProperty("equipment_rank", EquipmentRank);
    }
}
