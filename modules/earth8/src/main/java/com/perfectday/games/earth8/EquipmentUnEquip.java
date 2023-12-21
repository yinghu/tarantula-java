package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.perfectday.games.earth8.analytics.EquipmentUnequipTransaction;

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

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session){
        pendingAnalytics.add(new EquipmentUnequipTransaction(session, equipmentId, unitId));
        return true;
    }
}
