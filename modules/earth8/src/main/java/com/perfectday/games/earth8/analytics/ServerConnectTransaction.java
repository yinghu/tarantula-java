package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;
import com.icodesoftware.util.JsonUtil;

public class ServerConnectTransaction extends AnalyticsTransaction {
    private static final String MESSAGE_TYPE = "/core/server/0.0.1/connect";

    public ServerConnectTransaction(Session session, byte[] clientData)
    {
        super(MESSAGE_TYPE, session);
//        var object = JsonUtil.parse(clientData);
//        data.add("client_version", object.get("Version"));
    }
}
