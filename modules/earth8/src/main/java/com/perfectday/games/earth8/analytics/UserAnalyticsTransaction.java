package com.perfectday.games.earth8.analytics;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;

import java.time.LocalDateTime;

public class UserAnalyticsTransaction extends AnalyticsTransaction {
    public UserAnalyticsTransaction(String messageType, Session session, long serverSessionId)
    {
        super(messageType);
        data.addProperty("server_session_id", serverSessionId);
        data.addProperty("player_id", session.distributionId());
    }
}
