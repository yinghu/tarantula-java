package com.tarantula.platform.presence.pvp;

import com.google.gson.JsonObject;

import java.time.LocalDateTime;

public class PVPAnalytic {

    protected JsonObject data;

    public PVPAnalytic(String messageType)
    {
        data = new JsonObject();
        data.addProperty("message_type", messageType);
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
