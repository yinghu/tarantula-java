package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.service.ApplicationPreSetup;

public class UnitXpUp extends BattleUpdate{

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

    public static UnitXpUp fromJson(JsonObject jsonObject){
        UnitXpUp unitXpUp = new UnitXpUp();
        unitXpUp.parse(jsonObject);
        unitXpUp.xpGain = jsonObject.get("XpGain").getAsInt();
        return unitXpUp;
    }

    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup){
        return true;
    }
}
