package com.perfectday.games.earth8;

import com.google.gson.JsonObject;

public class EquipmentXpUp extends BattleUpdate{

    public int xpGain;
    @Override
    public boolean write(DataBuffer buffer) {
        if(!super.write(buffer)) return false;
        buffer.writeInt(xpGain);
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        super.read(buffer);
        xpGain = buffer.readInt();
        return true;
    }


    @Override
    public int getClassId() {
        return Earth8PortableRegistry.EQUIPMENT_XP_UP_CID;
    }

    @Override
    public int getFactoryId() {
        return Earth8PortableRegistry.OID;
    }

    public static EquipmentXpUp fromJson(JsonObject jsonObject){
        EquipmentXpUp unitRankUp = new EquipmentXpUp();
        unitRankUp.parse(jsonObject);
        unitRankUp.xpGain = jsonObject.get("XpGain").getAsInt();
        return unitRankUp;
    }

}
