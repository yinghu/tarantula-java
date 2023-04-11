package com.icodesoftware.integration.udp;

import com.icodesoftware.Session;
import com.icodesoftware.protocol.GameServiceProxy;
import com.icodesoftware.protocol.MessageBuffer;

public class EmptyGameServiceProxy implements GameServiceProxy {
    @Override
    public short serviceId() {
        return 0;
    }

    @Override
    public byte[] onService(Session session, byte[] payload) {
        return null;
    }

    @Override
    public byte[] onService(Session session, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        return null;
    }
}
