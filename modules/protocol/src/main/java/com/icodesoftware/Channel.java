package com.icodesoftware;

public interface Channel extends Configurable,Resettable{

    int channelId();
    int sessionId();
    int timeout();
    byte[] serverKey();

    void write(Session.Header messageHeader,byte[] payload);

    Connection connection();

    void close();

}
