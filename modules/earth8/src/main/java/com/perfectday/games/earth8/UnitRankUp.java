package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.service.ApplicationPreSetup;

public class UnitRankUp extends BattleUpdate{

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
        return Earth8PortableRegistry.UNIT_RANK_UP_CID;
    }


    public static UnitRankUp fromJson(JsonObject jsonObject){
        UnitRankUp unitRankUp = new UnitRankUp();
        unitRankUp.parse(jsonObject);
        unitRankUp.rank = jsonObject.get("Rank").getAsInt();
        return unitRankUp;
    }

}
