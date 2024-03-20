package com.perfectday.games.earth8;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.perfectday.games.earth8.analytics.BatchedManualAnalyticsTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ManualAnalyticsBatch extends BattleUpdate {

    private final UUID analyticsBatchId = UUID.randomUUID();
    private final List<AnalyticsData> analytics = new ArrayList<>();

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.MANUAL_ANALYTICS_BATCH_CID;
    }

    public static ManualAnalyticsBatch fromJson(JsonObject jsonObject) {
        ManualAnalyticsBatch manualAnalyticsBatch = new ManualAnalyticsBatch();
        manualAnalyticsBatch.parse(jsonObject);
        if (jsonObject.has("analytics")) {
            for (JsonElement jsonElement : jsonObject.getAsJsonArray("analytics")) {
                var updateJson = jsonElement.getAsJsonObject();
                var messageCategory = updateJson.get("messageCategory").getAsString();
                var messageType = updateJson.get("messageType").getAsString();
                updateJson.remove("messageCategory");
                updateJson.remove("messageType");
                manualAnalyticsBatch.analytics.add(new AnalyticsData(messageCategory, messageType, updateJson));
            }
        }
        return manualAnalyticsBatch;
    }

    private static class AnalyticsData {
        public String messageCategory;
        public String messageType;
        public JsonObject clientData;

        public AnalyticsData(String messageCategory, String messageType, JsonObject clientData) {
            this.messageCategory = messageCategory;
            this.messageType = messageType;
            this.clientData = clientData;
        }
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session) {
        pendingAnalytics.addAll(
                analytics.stream()
                        .map(analyticsData -> new BatchedManualAnalyticsTransaction(
                                session,
                                analyticsData.messageCategory,
                                analyticsData.messageType,
                                analyticsBatchId,
                                analyticsData.clientData))
                        .collect(Collectors.toList()));
        return true;
    }
}
