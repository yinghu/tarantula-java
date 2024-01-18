package com.perfectday.games.earth8.analytics;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;
import com.perfectday.games.earth8.BattleUpdate;

public class ClientAnalyticsTransaction extends AnalyticsTransaction {
    public ClientAnalyticsTransaction(Session session, String messageType, JsonObject clientData) {
        super(messageType, session);
        for (String key : clientData.keySet()) {
            data.addProperty(key, clientData.get(key).getAsString());
        }
    }
}
