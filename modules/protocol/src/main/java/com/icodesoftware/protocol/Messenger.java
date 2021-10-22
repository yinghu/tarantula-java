package com.icodesoftware.protocol;

import java.net.SocketAddress;

public interface Messenger {

    void send(MessageBuffer messageBuffer, SocketAddress destination);
}
