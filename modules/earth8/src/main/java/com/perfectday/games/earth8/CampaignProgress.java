package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.util.JsonUtil;
import com.perfectday.games.earth8.analytics.CampaignProgressTransaction;

public class CampaignProgress extends BattleUpdate{
    public String stageId;
    public long battleId;
    public int stars;

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.CAMPAIGN_PROGRESS_CID;
    }

    public static CampaignProgress fromJson(JsonObject jsonObject){
        CampaignProgress progress = new CampaignProgress();
        progress.parse(jsonObject);
        progress.stageId = JsonUtil.getJsonString(jsonObject, "StageID", "");
        progress.battleId = JsonUtil.getJsonLong(jsonObject, "BattleId", 0);
        progress.stars = JsonUtil.getJsonInt(jsonObject, "Stars", 0);
        return progress;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session,long serverSessionId,long batchId) {
        pendingAnalytics.add(new CampaignProgressTransaction(session,serverSessionId, battleId, stageId, stars));
        return true;
    }
}
