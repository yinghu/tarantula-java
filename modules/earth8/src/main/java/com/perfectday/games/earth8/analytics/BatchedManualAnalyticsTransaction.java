package com.perfectday.games.earth8.analytics;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;


public class BatchedManualAnalyticsTransaction extends AnalyticsTransaction {
    public BatchedManualAnalyticsTransaction(
            Session session,
            long serverSessionId,
            String messageCategory,
            String messageType,
            long analyticsBatchId,
            JsonObject clientData
    ) {
        super(String.format("/earth8/%s/0.0.1/%s", messageCategory, messageType), session,serverSessionId);
        data.addProperty("analyticsBatchId", analyticsBatchId);
        for (String key : clientData.keySet()) {
            data.add(key, clientData.get(key));
        }
    }
}
