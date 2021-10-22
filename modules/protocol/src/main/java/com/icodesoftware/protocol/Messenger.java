package com.icodesoftware.protocol;

public interface Messenger {

    void send(MessageBuffer.MessageHeader messageHeader,byte[] payload);
}
