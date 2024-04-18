package com.perfectday.games.earth8.analytics;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;

import java.time.LocalDateTime;

public class AnalyticsTransaction extends RecoverableObject {

    protected JsonObject data;


    public AnalyticsTransaction(String messageType, Session session,long serverSessionId)
    {
        data = new JsonObject();
        data.addProperty("server_session_id", serverSessionId);
        data.addProperty("message_type", messageType);
        data.addProperty("player_id", session.distributionId());
        data.addProperty("timestamp", LocalDateTime.now().toString());
    }

    @Override
    public java.lang.String toString() {
        return data.toString();
    }

    public byte[] toBytes() {
        return toString().getBytes();
    }

    
}
