package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;

public class EquipmentXpUpTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/equipment/0.0.1/xpUp";

    public EquipmentXpUpTransaction(Session session, long equipmentId, int xpGain)
    {
        super(MESSAGE_TYPE, session);
        data.addProperty("equipment_id", equipmentId);
        data.addProperty("xpGain", xpGain);
    }
}
