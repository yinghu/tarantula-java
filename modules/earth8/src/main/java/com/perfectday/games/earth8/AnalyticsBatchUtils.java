package com.perfectday.games.earth8;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.perfectday.games.earth8.analytics.BatchedManualAnalyticsTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AnalyticsBatchUtils {
    public static List<BatchedManualAnalyticsTransaction> getTransactions(Session session, UUID analyticsBatchId, List<AnalyticsData> analytics) {
        return analytics.stream()
                .map(analyticsData -> new BatchedManualAnalyticsTransaction(
                        session,
                        analyticsData.messageCategory,
                        analyticsData.messageType,
                        analyticsBatchId,
                        analyticsData.clientData))
                .collect(Collectors.toList());
    }

    public static List<AnalyticsData> getAnalyticsData(JsonArray analyticsJsonArray) {
        List<AnalyticsData> result = new ArrayList<>();
        for (JsonElement jsonElement : analyticsJsonArray) {
            var updateJson = jsonElement.getAsJsonObject();
            var messageCategory = updateJson.get("messageCategory").getAsString();
            var messageType = updateJson.get("messageType").getAsString();
            updateJson.remove("messageCategory");
            updateJson.remove("messageType");
            result.add(new AnalyticsData(messageCategory, messageType, updateJson));
        }
        return result;
    }

    static UUID generateAnalyticsBatchId() {
        return UUID.randomUUID();
    }

    public static class AnalyticsData {
        public String messageCategory;
        public String messageType;
        public JsonObject clientData;

        public AnalyticsData(String messageCategory, String messageType, JsonObject clientData) {
            this.messageCategory = messageCategory;
            this.messageType = messageType;
            this.clientData = clientData;
        }
    }
}
