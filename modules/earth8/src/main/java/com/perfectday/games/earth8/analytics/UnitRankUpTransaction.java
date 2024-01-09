package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;

public class UnitRankUpTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/unit/0.0.1/rankUp";

    public UnitRankUpTransaction(Session session, long unitId, int newRank, String unitName)
    {
        super(MESSAGE_TYPE, session);
        data.addProperty("unit_rank", newRank);
        data.addProperty("unit_id", unitId);
        data.addProperty("unit_name", unitName);
    }
}
