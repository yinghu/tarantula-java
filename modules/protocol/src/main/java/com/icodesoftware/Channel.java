package com.icodesoftware;

import com.icodesoftware.protocol.MessageBuffer;

public interface Channel extends Configurable,Resettable{

    int channelId();
    int sessionId();
    int timeout();
    byte[] serverKey();

    void write(MessageBuffer.MessageHeader messageHeader,byte[] payload);

    Connection connection();

    void close();

}
