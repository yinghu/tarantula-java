package com.icodesoftware.protocol;

import java.net.SocketAddress;

public interface Messenger {

    short ACK = 0;

    short JOIN = 100;
    short PING = 101;
    short REQUEST = 102;
    short LEAVE =103;

    //SERVER PUSH NOTIFICATION
    short ON_JOIN  = 200;
    short ON_REQUEST = 202;
    short ON_LEAVE = 203;

    //SERVER PENDING ACTION
    short ON_PENDING_ACTION = 500;

    void send(byte[] data,SocketAddress destination);
    void queue(byte[] data,SocketAddress destination);
}
