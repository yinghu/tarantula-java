package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;

public class ServerDisconnectTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/core/server/0.0.1/disconnect";

    public ServerDisconnectTransaction(Session session)
    {
        super(MESSAGE_TYPE, session);
    }
}
