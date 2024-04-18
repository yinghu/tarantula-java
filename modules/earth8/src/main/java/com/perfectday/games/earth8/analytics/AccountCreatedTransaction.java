package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;

public class AccountCreatedTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/core/account/0.0.1/created";

    public AccountCreatedTransaction(Session session,long serverSessionId)
    {
        super(MESSAGE_TYPE, session,serverSessionId);
    }
}
