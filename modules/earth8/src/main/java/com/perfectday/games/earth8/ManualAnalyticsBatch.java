package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;

import java.util.ArrayList;
import java.util.List;

public class ManualAnalyticsBatch extends BattleUpdate {


    public final List<AnalyticsBatchUtils.AnalyticsData> analytics = new ArrayList<>();

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
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session,long serverSessionId,long batchId) {

        pendingAnalytics.addAll(AnalyticsBatchUtils.getTransactions(session,serverSessionId,batchId, analytics));

        return true;
    }
}
