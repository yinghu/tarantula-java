package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.perfectday.games.earth8.analytics.AnalyticsEquipmentData;
import com.perfectday.games.earth8.analytics.EquipmentEquipTransaction;

public class EquipmentEquip extends BattleUpdate{

    private AnalyticsEquipmentData _equipmentData;
    private String _tempUnitName;

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.EQUIPMENT_EQUIP_CID;
    }


    public static EquipmentEquip fromJson(JsonObject jsonObject){
        EquipmentEquip self = new EquipmentEquip();
        self.parse(jsonObject);
        self._equipmentData = new AnalyticsEquipmentData(jsonObject);
        self._tempUnitName = GetJsonString(jsonObject, "TEMP_UnitName", "unknown");
        return self;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session){
        pendingAnalytics.add(new EquipmentEquipTransaction(session, _equipmentData, equipmentId, unitId, _tempUnitName));
        return true;
    }

}
