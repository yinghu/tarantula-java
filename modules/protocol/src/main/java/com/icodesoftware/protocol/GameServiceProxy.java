package com.icodesoftware.protocol;

import com.icodesoftware.Initializer;
import com.icodesoftware.Session;

public interface GameServiceProxy extends Initializer {

    short serviceId();

    boolean exported();

    //from http endpoint
    byte[] onService(Session session, byte[] payload);

    //from udp endpoint

    byte[] onService(Session stub, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer);

}
