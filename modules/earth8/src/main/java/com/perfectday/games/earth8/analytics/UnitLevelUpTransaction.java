package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;

public class UnitLevelUpTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/unit/0.0.1/levelUp";

    public UnitLevelUpTransaction(Session session, long serverSessionId,long unitId, int prevLevel, int newLevel, String unitName)
    {
        super(MESSAGE_TYPE, session,serverSessionId);
        data.addProperty("unit_id", unitId);
        data.addProperty("unit_level", newLevel);
        data.addProperty("old_unit_level", prevLevel);
        data.addProperty("unit_name", unitName);
    }
}
