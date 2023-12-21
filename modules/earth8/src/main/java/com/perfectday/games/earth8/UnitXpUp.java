package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.perfectday.games.earth8.analytics.UnitLevelUpTransaction;
import com.perfectday.games.earth8.analytics.UnitXpUpTransaction;

public class UnitXpUp extends BattleUpdate{

    public int xpGain;
    public int fromLevel;
    public int toLevel;

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
        unitXpUp.xpGain = GetJsonInt(jsonObject, "XpGain", 0);
        unitXpUp.fromLevel = GetJsonInt(jsonObject, "FromLevel", 0);
        unitXpUp.toLevel = GetJsonInt(jsonObject, "ToLevel", 0);
        return unitXpUp;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session){
        pendingAnalytics.add(new UnitXpUpTransaction(session, unitId, 0));
        if(toLevel > fromLevel)
        {
            pendingAnalytics.add(new UnitLevelUpTransaction(session, unitId, fromLevel, toLevel));
        }
        return true;
    }
}
