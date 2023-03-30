package com.icodesoftware.protocol;


import com.icodesoftware.Channel;
import com.icodesoftware.Connection;
import com.icodesoftware.Session;

import com.icodesoftware.util.RecoverableObject;

public class ChannelHeader extends RecoverableObject implements Channel {

    protected String configurationTypeId;

    protected int channelId;
    protected int sessionId;
    protected int timeout;
    protected byte[] serverKey;
    protected Connection connection;

    public String configurationTypeId() {
        return this.configurationTypeId;
    }

    public void configurationTypeId(String configurationTypeId) {
        this.configurationTypeId = configurationTypeId;
    }


    @Override
    public int channelId() {
        return channelId;
    }

    @Override
    public int sessionId() {
        return sessionId;
    }

    @Override
    public int timeout() {
        return timeout;
    }

    @Override
    public byte[] serverKey() {
        return serverKey;
    }

    @Override
    public void write(Session.Header header, byte[] payload) {
    }

    @Override
    public Connection connection() {
        return connection;
    }

    @Override
    public void close() {

    }

    @Override
    public void reset() {

    }
}
