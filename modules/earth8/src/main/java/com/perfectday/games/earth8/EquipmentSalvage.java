package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.perfectday.games.earth8.analytics.EquipmentSalvageTransaction;

public class EquipmentSalvage extends BattleUpdate{

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.EQUIPMENT_SALVAGE_CID;
    }


    public static EquipmentSalvage fromJson(JsonObject jsonObject){
        EquipmentSalvage salvage = new EquipmentSalvage();
        salvage.parse(jsonObject);
        salvage.equipmentId = jsonObject.get("EquipmentId").getAsLong();
        return salvage;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session){
        pendingAnalytics.add(new EquipmentSalvageTransaction(session, equipmentId));
        return true;
    }

}
