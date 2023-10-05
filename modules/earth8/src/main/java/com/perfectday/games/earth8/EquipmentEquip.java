package com.perfectday.games.earth8;

import com.google.gson.JsonObject;

public class EquipmentEquip extends BattleUpdate{

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.EQUIPMENT_EQUIP_CID;
    }

    @Override
    public int getFactoryId() {
        return Earth8PortableRegistry.OID;
    }

    public static EquipmentEquip fromJson(JsonObject jsonObject){
        EquipmentEquip unitRankUp = new EquipmentEquip();
        unitRankUp.parse(jsonObject);
        return unitRankUp;
    }

}
