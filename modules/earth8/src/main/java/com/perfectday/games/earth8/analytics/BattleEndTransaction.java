package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;

public class BattleEndTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/player/0.0.1/battleEnd";

    public BattleEndTransaction(Session session, long battleId, byte[] clientData)
    {
        super(MESSAGE_TYPE, session);
        var object = JsonUtil.parse(clientData);
        data.addProperty("battle_id", battleId);
        data.add("win", object.get("Win"));
    }
}
