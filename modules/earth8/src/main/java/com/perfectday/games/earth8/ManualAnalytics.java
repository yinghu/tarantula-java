package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.perfectday.games.earth8.analytics.CampaignProgressTransaction;
import com.perfectday.games.earth8.analytics.ClientAnalyticsTransaction;

import java.util.Map;

public class ManualAnalytics extends BattleUpdate{
    public String messageType = "UNKNOWN";
    public JsonObject clientData = new JsonObject();

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.MANUAL_ANALYTICS_CID;
    }

    public static ManualAnalytics fromJson(JsonObject jsonObject){
        ManualAnalytics manualAnalytics = new ManualAnalytics();
        manualAnalytics.parse(jsonObject);
        if (jsonObject.has("messageType")) {
            manualAnalytics.messageType = jsonObject.get("messageType").getAsString();
        }
        if (jsonObject.has("clientData")) {
            manualAnalytics.clientData = jsonObject.get("clientData").getAsJsonObject();
        }
        return manualAnalytics;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session) {
        pendingAnalytics.add(new ClientAnalyticsTransaction(session, messageType, clientData));
        return true;
    }
}
