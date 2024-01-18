package com.perfectday.games.earth8.analytics;

import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;

import java.time.LocalDateTime;

public class ServerMetadataTransaction {
    protected JsonObject data;

    private static final String MESSAGE_TYPE = "/core/server/0.0.1/metadata";

    public ServerMetadataTransaction(OnAccess event)
    {
        data = event.toJson();
        data.addProperty("message_type", MESSAGE_TYPE);
        data.addProperty("player_id", 0);
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
