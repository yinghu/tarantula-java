package com.icodesoftware;

import com.icodesoftware.protocol.MessageBuffer;

public interface Channel extends Configurable{

    int channelId();
    int sessionId();
    void write(MessageBuffer.MessageHeader messageHeader,byte[] payload);

    Connection connection();

}
