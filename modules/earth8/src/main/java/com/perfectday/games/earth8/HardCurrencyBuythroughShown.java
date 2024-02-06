package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.perfectday.games.earth8.analytics.HardCurrencyBuythroughShownTransaction;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.TarantulaLogger;

public class HardCurrencyBuythroughShown extends BattleUpdate{
    public String currency;
    public String trigger;

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.CLIENT_HC_BUYTHROUGH_SHOWN;
    }

    public static HardCurrencyBuythroughShown fromJson(JsonObject jsonObject){
        HardCurrencyBuythroughShown buythroughShown = new HardCurrencyBuythroughShown();
        buythroughShown.parse(jsonObject);
        buythroughShown.currency = jsonObject.get("currency").getAsString();
        buythroughShown.trigger = jsonObject.get("trigger").getAsString();
        
        return buythroughShown;
    }
    
    private static TarantulaLogger log = JDKLogger.getLogger(HardCurrencyBuythroughShown.class);

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session) {
        HardCurrencyBuythroughShownTransaction transaction = new HardCurrencyBuythroughShownTransaction(session, currency, trigger);
        pendingAnalytics.add(transaction);
        log.info(transaction.toString());
        return true;
    }
}