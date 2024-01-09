package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;
import com.perfectday.games.earth8.BattleUpdate;

public class BattleEndTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/player/0.0.1/battleEnd";

    public BattleEndTransaction(Session session, long battleId, byte[] clientData)
    {
        super(MESSAGE_TYPE, session);
        var object = JsonUtil.parse(clientData);
        data.addProperty("battle_id", battleId);

        var win = BattleUpdate.GetJsonBool(object, "Win", false);
        data.addProperty("outcome", win ? "win" : "loss");
        var fastSpeed = BattleUpdate.GetJsonBool(object,"EndedWithFastSpeed", false);
        data.addProperty("battle_speed_x2", fastSpeed);
    }
}
