package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;

public class UnitXpUpTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/unit/0.0.1/xpUp";

    public UnitXpUpTransaction(Session session, long unitId, int xpGain, String unitName)
    {
        super(MESSAGE_TYPE, session);
        data.addProperty("unit_id", unitId);
        data.addProperty("xpGain", xpGain);
        data.addProperty("unit_name", unitName);
    }
}
