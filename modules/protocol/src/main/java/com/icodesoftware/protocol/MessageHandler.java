package com.icodesoftware.protocol;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public interface MessageHandler {

    int ACK = 0;
    int JOIN = 1;
    int ECHO = 2;
    int RELAY = 3;
    int LEAVE = 4;
    int SPAWN = 5;
    int PING = 6;
    int PONG = 7;
    int VOTE = 8;
    int SYNC = 9;

    int ON_JOINED = 10;
    int ON_LEFT = 11;
    int ON_KICKED_OFF = 12;

    int DISCHARGE = 100;
    int SERVER_PUSH = 200;

    int type();
    void onMessage(InboundMessage pendingInboundMessage);
    void relay();
}
