package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;

public class EquipmentSalvageTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/equipment/0.0.1/salvage";

    public EquipmentSalvageTransaction(Session session, long equipmentId)
    {
        super(MESSAGE_TYPE, session);
        data.addProperty("equipment_id", equipmentId);
    }
}
