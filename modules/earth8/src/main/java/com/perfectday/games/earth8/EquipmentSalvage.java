package com.perfectday.games.earth8;

import com.google.gson.JsonObject;

public class EquipmentSalvage extends BattleUpdate{


    @Override
    public int getClassId() {
        return Earth8PortableRegistry.EQUIPMENT_SALVAGE_CID;
    }

    @Override
    public int getFactoryId() {
        return Earth8PortableRegistry.OID;
    }

    public static EquipmentSalvage fromJson(JsonObject jsonObject){
        EquipmentSalvage unitRankUp = new EquipmentSalvage();
        unitRankUp.parse(jsonObject);
        return unitRankUp;
    }

}
