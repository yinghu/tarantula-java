package com.tarantula.platform.presence.pvp;


public class PVPRatingChangeAnalytic extends PVPAnalytic{

    private static final String MESSAGE_TYPE = "/earth8/pvp/0.0.1/PVPRatingChange";

    public PVPRatingChangeAnalytic(long playerId, int ratingDelta, int newRating, String league, long battleId, boolean attacking)
    {
        super(MESSAGE_TYPE);
        data.addProperty("player_id", playerId);
        data.addProperty("attacking", attacking);
        data.addProperty("rating_delta", ratingDelta);
        data.addProperty("new_rating",newRating);
        data.addProperty("league", league);
        data.addProperty("battle_id", battleId);
    }
}
