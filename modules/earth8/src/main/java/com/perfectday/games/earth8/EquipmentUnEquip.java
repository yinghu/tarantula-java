package com.perfectday.games.earth8;

import com.google.gson.JsonObject;

public class EquipmentUnEquip extends BattleUpdate{


    @Override
    public int getClassId() {
        return Earth8PortableRegistry.EQUIPMENT_UN_EQUIP_CID;
    }

    public static EquipmentUnEquip fromJson(JsonObject jsonObject){
        EquipmentUnEquip unitRankUp = new EquipmentUnEquip();
        unitRankUp.parse(jsonObject);
        return unitRankUp;
    }

}
