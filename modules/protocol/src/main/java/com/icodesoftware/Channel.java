package com.icodesoftware;

public interface Channel {

    int channelId();
    int sessionId();
    void write(byte[] payload);

    Connection connection();

}
