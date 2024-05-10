package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;

public class CurrencyUpdateTransaction extends UserAnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/earth8/inventory/0.0.1/currencyUpdate";

    public CurrencyUpdateTransaction(Session session,long serverSessionId, String context, String currencyId, int currencyDelta, int currencyTotal, String fakeTransactionId)
    {
        super(MESSAGE_TYPE, session,serverSessionId);
        data.addProperty("context", context);
        data.addProperty("currency_id", currencyId);
        data.addProperty("currency_delta", currencyDelta);
        data.addProperty("currency_total", currencyTotal);
        data.addProperty("fake_transaction_id", fakeTransactionId);
    }
}
