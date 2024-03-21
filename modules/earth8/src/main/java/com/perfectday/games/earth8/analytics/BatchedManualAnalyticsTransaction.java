package com.perfectday.games.earth8.analytics;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;

import java.util.UUID;

public class BatchedManualAnalyticsTransaction extends AnalyticsTransaction {
    public BatchedManualAnalyticsTransaction(
            Session session,
            String messageCategory,
            String messageType,
            UUID analyticsBatchId,
            JsonObject clientData
    ) {
        super(String.format("/earth8/%s/0.0.1/%s", messageCategory, messageType), session);
        data.addProperty("analyticsBatchId", analyticsBatchId.toString());
        for (String key : clientData.keySet()) {
            data.add(key, clientData.get(key));
        }
    }
}
