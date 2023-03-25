package com.icodesoftware.protocol;

import com.icodesoftware.Session;

public interface GameServiceProxy{

    short serviceId();

    //from http endpoint
    byte[] onService(Session session, byte[] payload);

    //from udp endpoint
    byte[] onService(Session session, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer);

}
