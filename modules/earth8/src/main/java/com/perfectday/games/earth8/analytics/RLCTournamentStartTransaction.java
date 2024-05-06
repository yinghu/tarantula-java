package com.perfectday.games.earth8.analytics;

public class RLCTournamentStartTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/tournament/0.0.1/tournamentStart";
    
    public RLCTournamentStartTransaction(long rlcId, String tournamentName)
    {
        super(MESSAGE_TYPE);
        data.addProperty("RLC_Id", rlcId);
        data.addProperty("Tournament_Name", tournamentName);
    }
}
