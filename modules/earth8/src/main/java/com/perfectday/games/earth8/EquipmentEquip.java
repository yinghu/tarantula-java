package com.perfectday.games.earth8;

import com.google.gson.JsonObject;

public class EquipmentEquip extends BattleUpdate{

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.EQUIPMENT_EQUIP_CID;
    }


    public static EquipmentEquip fromJson(JsonObject jsonObject){
        EquipmentEquip unitRankUp = new EquipmentEquip();
        unitRankUp.parse(jsonObject);
        return unitRankUp;
    }

}
