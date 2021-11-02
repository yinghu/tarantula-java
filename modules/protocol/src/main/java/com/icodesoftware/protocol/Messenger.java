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

    //Message deliver mode channel-broadcasting-no-sender 0, channel-broadcasting 1, sender-request 2
    short CHANNEL_BROADCASTING_NO_SENDER = 0;
    short CHANNEL_BROADCASTING = 1;
    short SENDER_REQUEST = 2;

    void send(byte[] data,SocketAddress destination);
}
