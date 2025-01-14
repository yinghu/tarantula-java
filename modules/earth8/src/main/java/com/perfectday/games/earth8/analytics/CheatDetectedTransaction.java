package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;
import com.perfectday.games.earth8.AnalyticsBatchUtils;

import java.time.LocalDateTime;

public class CheatDetectedTransaction extends UserAnalyticsTransaction{

    private static final String MESSAGE_TYPE = "/earth8/player/0.0.1/cheatDetected";

    public CheatDetectedTransaction(Session session, long serverSessionId, int maxValueExceeded, AnalyticsBatchUtils.AnalyticsData analyticsData)
    {
        super(MESSAGE_TYPE, session,serverSessionId);
        data.addProperty("max_value_exceeded", maxValueExceeded);
        data.add("original_analytic", analyticsData.clientData);
    }
}
