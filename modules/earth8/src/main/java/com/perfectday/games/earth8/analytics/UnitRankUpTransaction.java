package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;

public class UnitRankUpTransaction extends UserAnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/unit/0.0.1/rankUp";

    public UnitRankUpTransaction(Session session,long serverSessionId, long unitId, int newRank, String unitName)
    {
        super(MESSAGE_TYPE, session,serverSessionId);
        data.addProperty("unit_rank", newRank);
        data.addProperty("unit_id", unitId);
        data.addProperty("unit_name", unitName);
    }
}
