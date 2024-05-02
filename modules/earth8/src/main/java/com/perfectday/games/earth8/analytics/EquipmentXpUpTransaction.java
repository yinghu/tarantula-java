package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;

public class EquipmentXpUpTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/equipment/0.0.1/xpUp";

    public EquipmentXpUpTransaction(Session session, long serverSessionId,AnalyticsEquipmentData equipmentData, long equipmentId, int xpGain)
    {
        super(MESSAGE_TYPE, session,serverSessionId);
        data.addProperty("equipment_id", equipmentId);
        data.addProperty("xpGain", xpGain);
        equipmentData.addToObject(data);
    }
}
