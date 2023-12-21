package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;

public class EquipmentEquipTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/equipment/0.0.1/equip";

    public EquipmentEquipTransaction(Session session, long equipmentId, long unitId)
    {
        super(MESSAGE_TYPE, session);
        data.addProperty("equipment_id", equipmentId);
        data.addProperty("unit_id", unitId);
    }
}
