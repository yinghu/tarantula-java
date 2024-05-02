package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.perfectday.games.earth8.analytics.CurrencyUpdateTransaction;

public class CurrencyUpdate extends BattleUpdate{
    public String context;
    public String currencyId;
    public int currencyDelta;
    public int currencyTotal;
    public String fakeTransactionId;

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.CURRENCY_UPDATE_CID;
    }

    public static CurrencyUpdate fromJson(JsonObject jsonObject){
        CurrencyUpdate currencyUpdate = new CurrencyUpdate();
        currencyUpdate.parse(jsonObject);
        currencyUpdate.context = jsonObject.get("context").getAsString();
        currencyUpdate.currencyId = jsonObject.get("currencyId").getAsString();
        currencyUpdate.currencyDelta = jsonObject.get("currencyDelta").getAsInt();
        currencyUpdate.currencyTotal = jsonObject.get("currencyTotal").getAsInt();
        currencyUpdate.fakeTransactionId = jsonObject.get("fakeTransactionId").getAsString();

        return currencyUpdate;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session,long serverSessionId,long batchId) {
        pendingAnalytics.add(new CurrencyUpdateTransaction(session,serverSessionId, context, currencyId, currencyDelta, currencyTotal, fakeTransactionId));
        return true;
    }
}
