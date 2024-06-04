package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;

public class RLCPointsEarnedTransaction extends UserAnalyticsTransaction{
    private static final String MESSAGE_TYPE = "/earth8/tournament/0.0.1/tournamentPointsEarned";

    public RLCPointsEarnedTransaction(Session session, long serverSessionId, long rlcId, String objectiveType, int pointsEarned, double totalPoints) {
        super(MESSAGE_TYPE, session, serverSessionId);
        data.addProperty("RLC_Id", rlcId);
        data.addProperty("Objective_Type", objectiveType);
        data.addProperty("Points_Earned", pointsEarned);
        data.addProperty("Total_Points", totalPoints);
    }
}
