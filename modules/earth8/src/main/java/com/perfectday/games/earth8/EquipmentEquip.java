package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.perfectday.games.earth8.analytics.EquipmentEquipTransaction;

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

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session){
        pendingAnalytics.add(new EquipmentEquipTransaction(session, equipmentId, unitId));
        return true;
    }

}
