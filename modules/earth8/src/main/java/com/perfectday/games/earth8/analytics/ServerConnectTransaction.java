package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;

public class ServerConnectTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/core/player/0.0.1/connect";

    public ServerConnectTransaction(Session session,long serverSessionId)
    {
        super(MESSAGE_TYPE, session,serverSessionId);

    }

}
