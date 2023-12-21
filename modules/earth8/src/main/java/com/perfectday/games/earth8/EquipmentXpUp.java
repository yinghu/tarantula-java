package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.perfectday.games.earth8.analytics.EquipmentLevelUpTransaction;
import com.perfectday.games.earth8.analytics.EquipmentXpUpTransaction;

public class EquipmentXpUp extends BattleUpdate{

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
        return Earth8PortableRegistry.EQUIPMENT_XP_UP_CID;
    }


    public static EquipmentXpUp fromJson(JsonObject jsonObject){
        EquipmentXpUp unitRankUp = new EquipmentXpUp();
        unitRankUp.parse(jsonObject);
        unitRankUp.xpGain = GetJsonInt(jsonObject, "XpGain", 0);
        unitRankUp.fromLevel = GetJsonInt(jsonObject, "FromLevel", 0);
        unitRankUp.toLevel = GetJsonInt(jsonObject, "ToLevel", 0);
        return unitRankUp;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session){
        pendingAnalytics.add(new EquipmentXpUpTransaction(session, equipmentId, xpGain));
        if(toLevel > fromLevel)
        {
            pendingAnalytics.add(new EquipmentLevelUpTransaction(session, equipmentId, fromLevel, toLevel));
        }
        return true;
    }

}
