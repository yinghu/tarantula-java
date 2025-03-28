package com.tarantula.platform.presence.pvp;

public class PVPLeagueChangeAnalytic extends PVPAnalytic{

    private static final String MESSAGE_TYPE = "/earth8/pvp/0.0.1/PVPLeagueChange";

    public PVPLeagueChangeAnalytic(long playerId, String oldLeague, String newLeague, int elo, long battleId)
    {
        super(MESSAGE_TYPE);
        data.addProperty("player_id", playerId);
        data.addProperty("old_league", oldLeague);
        data.addProperty("new_league", newLeague);
        data.addProperty("elo", elo);
        data.addProperty("battle_id", battleId);
    }
}
