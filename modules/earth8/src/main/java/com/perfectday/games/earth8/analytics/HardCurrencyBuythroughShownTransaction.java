package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;
import com.perfectday.games.earth8.CurrencyUpdate;

public class HardCurrencyBuythroughShownTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/inventory/0.0.1/clientHCBuythroughShown";

    public HardCurrencyBuythroughShownTransaction(Session session, String currency, String trigger)
    {
        super(MESSAGE_TYPE, session);
        data.addProperty("currency", currency);
        data.addProperty("trigger", trigger);
    }
}
