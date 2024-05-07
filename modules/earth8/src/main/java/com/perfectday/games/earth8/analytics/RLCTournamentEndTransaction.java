package com.perfectday.games.earth8.analytics;

public class RLCTournamentEndTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/tournament/0.0.1/tournamentEnd";

    public RLCTournamentEndTransaction(long rlcId)
    {
        super(MESSAGE_TYPE);
        data.addProperty("RLC_Id", rlcId);
    }
}
