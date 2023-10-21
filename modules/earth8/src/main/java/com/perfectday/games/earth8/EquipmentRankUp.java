package com.perfectday.games.earth8;

import com.google.gson.JsonObject;

public class EquipmentRankUp extends BattleUpdate{

    public int rank;
    @Override
    public boolean write(DataBuffer buffer) {
        if(!super.write(buffer)) return false;
        buffer.writeInt(rank);
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        super.read(buffer);
        rank = buffer.readInt();
        return true;
    }


    @Override
    public int getClassId() {
        return Earth8PortableRegistry.EQUIPMENT_RANK_UP_CID;
    }


    public static EquipmentRankUp fromJson(JsonObject jsonObject){
        EquipmentRankUp unitRankUp = new EquipmentRankUp();
        unitRankUp.parse(jsonObject);
        unitRankUp.rank = jsonObject.get("Rank").getAsInt();
        return unitRankUp;
    }

}
