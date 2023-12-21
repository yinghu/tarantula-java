package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;

public class EquipmentLevelUpTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/equipment/0.0.1/levelUp";

    public EquipmentLevelUpTransaction(Session session, long equipmentId, int prevLevel, int newLevel)
    {
        super(MESSAGE_TYPE, session);
        data.addProperty("equipment_id", equipmentId);
        data.addProperty("equipment_level", newLevel);
        data.addProperty("old_equipment_level", prevLevel);
    }
}
