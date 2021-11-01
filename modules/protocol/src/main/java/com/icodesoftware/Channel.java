package com.icodesoftware;

public interface Channel extends Configurable{

    int channelId();
    int sessionId();
    void write(byte[] payload);

    Connection connection();

}
