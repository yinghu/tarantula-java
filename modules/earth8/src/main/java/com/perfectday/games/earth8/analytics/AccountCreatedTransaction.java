package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;

public class AccountCreatedTransaction extends UserAnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/core/account/0.0.1/created";

    public AccountCreatedTransaction(Session session,long serverSessionId)
    {
        super(MESSAGE_TYPE, session,serverSessionId);
    }
}
