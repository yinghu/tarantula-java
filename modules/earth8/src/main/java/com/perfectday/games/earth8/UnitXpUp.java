package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.perfectday.games.earth8.analytics.UnitLevelUpTransaction;

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

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.UNIT_XP_UP_CID;
    }


    public static UnitXpUp fromJson(JsonObject jsonObject){
        UnitXpUp unitXpUp = new UnitXpUp();
        unitXpUp.parse(jsonObject);
        unitXpUp.xpGain = jsonObject.get("XpGain").getAsInt();
        return unitXpUp;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session){
        pendingAnalytics.add(new UnitLevelUpTransaction(session, unitId, 0, 0));
        return true;
    }
}
