package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;

public class EquipmentSalvageTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/equipment/0.0.1/salvage";

    public EquipmentSalvageTransaction(Session session, long serverSessionId,AnalyticsEquipmentData equipmentData, long equipmentId)
    {
        super(MESSAGE_TYPE, session,serverSessionId);
        data.addProperty("equipment_id", equipmentId);
        equipmentData.addToObject(data);
    }
}
