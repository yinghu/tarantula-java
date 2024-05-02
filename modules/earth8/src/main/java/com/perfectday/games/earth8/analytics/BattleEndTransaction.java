package com.perfectday.games.earth8.analytics;

import com.google.gson.JsonNull;
import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;
import com.perfectday.games.earth8.BattleUpdate;

import java.util.UUID;

public class BattleEndTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/player/0.0.1/battleEnd";

    public BattleEndTransaction(Session session,long serverSessionId, long battleId, byte[] clientData, long analyticsBatchId)
    {
        super(MESSAGE_TYPE, session,serverSessionId);
        var object = JsonUtil.parse(clientData);
        data.addProperty("battle_id", battleId);

        var win = JsonUtil.getJsonBool(object, "Win", false);
        data.addProperty("outcome", win ? "win" : "loss");
        var fastSpeed = JsonUtil.getJsonBool(object,"EndedWithFastSpeed", false);
        data.addProperty("battle_speed_x2", fastSpeed);
        if(object.get("StarsEarned") != null)
        {
            data.addProperty("starsEarned", object.get("StarsEarned").getAsInt());
        }
        else
        {
            data.add("starsEarned", JsonNull.INSTANCE);
        }
        data.addProperty("analyticsBatchId", analyticsBatchId);
    }
}
