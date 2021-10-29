package com.icodesoftware.protocol;

import java.net.SocketAddress;

public interface Messenger {

    short ACK = 0;

    short JOIN = 100;
    short PING = 101;
    short LEAVE =102;

    //SERVER PUSH NOTIFICATION
    short ON_JOIN  = 200;
    short ON_LEAVE = 202;

    void send(byte[] data,SocketAddress destination);
}
