package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;
import com.perfectday.games.earth8.BattleUpdate;

public class ClientAnalyticsTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/player/0.0.1/clientAnalytics";
    public ClientAnalyticsTransaction(Session session, byte[] clientData) {
        super(MESSAGE_TYPE, session);

        var object = JsonUtil.parse(clientData);
        var msgType = BattleUpdate.GetJsonString(object, "message_type", "UNKNOWN");
        data.addProperty("message_type", msgType);
        var clientDataJson = object.getAsJsonObject("client_data");
        data.addProperty("client_data", clientDataJson.getAsString());
    }
}
