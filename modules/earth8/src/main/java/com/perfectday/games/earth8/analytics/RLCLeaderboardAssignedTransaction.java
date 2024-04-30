package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;

public class RLCLeaderboardAssignedTransaction extends UserAnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/tournament/0.0.1/tournamentLeaderboardAssigned";

    public RLCLeaderboardAssignedTransaction(Session session, long serverSessionId, long rlcId) {
        super(MESSAGE_TYPE, session, serverSessionId);
        data.addProperty("RLC_Id", rlcId);
    }
}