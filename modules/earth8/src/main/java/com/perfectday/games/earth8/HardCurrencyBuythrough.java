package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.perfectday.games.earth8.analytics.HardCurrencyBuythroughShownTransaction;
import com.perfectday.games.earth8.analytics.HardCurrencyBuythroughTransaction;

public class HardCurrencyBuythrough extends BattleUpdate{
    public String currencyPurchased;
    public int hCPrice;
    public int currencyTotal;
    public String trigger;
    public String transactionId;

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.INVENTORY_HC_BUYTHROUGH;
    }

    public static HardCurrencyBuythrough fromJson(JsonObject jsonObject){
        HardCurrencyBuythrough buythroughShown = new HardCurrencyBuythrough();
        buythroughShown.parse(jsonObject);
        buythroughShown.currencyPurchased = jsonObject.get("currencyPurchased").getAsString();
        buythroughShown.hCPrice = jsonObject.get("hCPrice").getAsInt();
        buythroughShown.currencyTotal = jsonObject.get("currencyTotal").getAsInt();
        buythroughShown.trigger = jsonObject.get("trigger").getAsString();
        buythroughShown.transactionId = jsonObject.get("transactionId").getAsString();
        
        return buythroughShown;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session,long serverSessionId,long batchId) {
        pendingAnalytics.add(new HardCurrencyBuythroughTransaction(session,serverSessionId, currencyPurchased, hCPrice, currencyTotal, trigger, transactionId));
        return true;
    }
}