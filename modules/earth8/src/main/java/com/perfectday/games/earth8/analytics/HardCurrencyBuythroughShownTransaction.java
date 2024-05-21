package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;

public class HardCurrencyBuythroughShownTransaction extends UserAnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/inventory/0.0.1/clientHCBuythroughShown";

    public HardCurrencyBuythroughShownTransaction(Session session, long serverSessionId,String currency, String trigger)
    {
        super(MESSAGE_TYPE, session,serverSessionId);
        data.addProperty("currency", currency);
        data.addProperty("trigger", trigger);
    }
}
