package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;

public class CampaignProgressTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/player/0.0.1/campaignProgress";

    public CampaignProgressTransaction(Session session,long serverSessionId, long battleId, String stageId, int stars)
    {
        super(MESSAGE_TYPE, session,serverSessionId);
        data.addProperty("battle_id", battleId);
        data.addProperty("stage_id", stageId);
        data.addProperty("stars", stars);
    }
}
