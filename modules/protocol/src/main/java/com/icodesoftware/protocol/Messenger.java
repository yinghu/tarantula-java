package com.icodesoftware.protocol;

import java.net.SocketAddress;

public interface Messenger {

    short ACK = 0;

    short JOIN = 100;
    short PING = 101;
    short REQUEST = 102;
    short LEAVE =103;

    //short RELAY = 105;
    short PLAY = 106;

    //SERVER PUSH NOTIFICATION
    short ON_JOIN  = 200;
    short ON_REQUEST = 202;
    short ON_LEAVE = 203;
    short ON_PUSH = 204;

    //short ON_RELAY = 205;
    short ON_PLAY = 206;


    byte[] buffer();
    void buffer(byte[] buffer);

    MessageBuffer messageBuffer();
    void messageBuffer(MessageBuffer messageBuffer);

    void queue(byte[] data,int length,SocketAddress destination);
    void queue(MessageBuffer messageBuffer,SocketAddress destination);

}
