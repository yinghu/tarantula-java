package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.perfectday.games.earth8.analytics.AnalyticsEquipmentData;
import com.perfectday.games.earth8.analytics.EquipmentSalvageTransaction;

public class EquipmentSalvage extends BattleUpdate{

    private AnalyticsEquipmentData _equipmentData;

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.EQUIPMENT_SALVAGE_CID;
    }


    public static EquipmentSalvage fromJson(JsonObject jsonObject){
        EquipmentSalvage self = new EquipmentSalvage();
        self.parse(jsonObject);
        self.equipmentId = jsonObject.get("EquipmentId").getAsLong();
        self._equipmentData = new AnalyticsEquipmentData(jsonObject);
        return self;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session){
        pendingAnalytics.add(new EquipmentSalvageTransaction(session, _equipmentData, equipmentId));
        return true;
    }

}
