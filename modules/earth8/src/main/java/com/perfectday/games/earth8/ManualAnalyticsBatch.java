package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ManualAnalyticsBatch extends BattleUpdate {

    private final UUID analyticsBatchId = AnalyticsBatchUtils.generateAnalyticsBatchId();

    private final List<AnalyticsBatchUtils.AnalyticsData> analytics = new ArrayList<>();

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.MANUAL_ANALYTICS_BATCH_CID;
    }

    public static ManualAnalyticsBatch fromJson(JsonObject jsonObject) {
        ManualAnalyticsBatch manualAnalyticsBatch = new ManualAnalyticsBatch();
        manualAnalyticsBatch.parse(jsonObject);
        if (jsonObject.has("analytics")) {
            manualAnalyticsBatch.analytics.addAll(AnalyticsBatchUtils.getAnalyticsData(
                    jsonObject.getAsJsonArray("analytics"))
            );
        }
        return manualAnalyticsBatch;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session) {
        pendingAnalytics.addAll(AnalyticsBatchUtils.getTransactions(session, analyticsBatchId, analytics));

        return true;
    }
}
