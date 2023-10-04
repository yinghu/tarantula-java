package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.util.JsonUtil;

public class UnitXpUp extends BattleUpdate{

    public int xpGain;
    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeLong(unitId);
        buffer.writeInt(xpGain);
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        unitId = buffer.readLong();
        xpGain = buffer.readInt();
        return true;
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("UnitId",unitId);
        jsonObject.addProperty("XpGain",xpGain);
        return jsonObject;
    }
    @Override
    public int getClassId() {
        return Earth8PortableRegistry.UNIT_XP_UP_CID;
    }

    @Override
    public int getFactoryId() {
        return Earth8PortableRegistry.OID;
    }

    public static UnitXpUp fromJson(byte[] payload){
        JsonObject jsonObject = JsonUtil.parse(payload);
        UnitXpUp unitXpUp = new UnitXpUp();
        unitXpUp.unitId = jsonObject.get("UnitId").getAsLong();
        unitXpUp.xpGain = jsonObject.get("XpGain").getAsInt();
        return unitXpUp;
    }
}
