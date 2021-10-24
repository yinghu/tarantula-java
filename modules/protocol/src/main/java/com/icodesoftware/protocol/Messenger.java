package com.icodesoftware.protocol;

import java.net.SocketAddress;

public interface Messenger {

    short ACK = 0;

    short JOIN = 100;
    short PING = 101;

    //SERVER PUSH NOTIFICATION
    short ON_JOIN  = 200;

    byte[] send(MessageBuffer messageBuffer, SocketAddress destination);
}
