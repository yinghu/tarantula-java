package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;

public class HardCurrencyBuythroughTransaction extends UserAnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/inventory/0.0.1/inventoryHCBuythrough";

    public HardCurrencyBuythroughTransaction(Session session,long serverSessionId, String currencyPurchased, int HCPrice, int currencyTotal, String trigger, String transactionID)
    {
        super(MESSAGE_TYPE, session,serverSessionId);
        data.addProperty("currencyPurchased", currencyPurchased);
        data.addProperty("hCPrice", HCPrice);
        data.addProperty("currencyTotal", currencyTotal);
        data.addProperty("trigger", trigger);
        data.addProperty("transactionID", transactionID);
    }
}
