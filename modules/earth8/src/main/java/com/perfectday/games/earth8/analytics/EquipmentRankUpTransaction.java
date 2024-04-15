package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;

public class EquipmentRankUpTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/equipment/0.0.1/rankUp";

    public EquipmentRankUpTransaction(Session session,long serverSessionId, AnalyticsEquipmentData equipmentData, long equipmentId, int rank)
    {
        super(MESSAGE_TYPE, session,serverSessionId);
        data.addProperty("equipment_id", equipmentId);
        data.addProperty("rank", rank);
        equipmentData.addToObject(data);
    }
}
