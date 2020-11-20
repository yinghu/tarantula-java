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
    int GAME = 10;

    int ON_JOINED = 100;
    int ON_LEFT = 101;
    int ON_KICKED_OFF = 102;


    int SERVER_PUSH = 200;
    int GAME_SPEC = 201;
    int GAME_START = 202;
    int GAME_CLOSE = 203;
    int GAME_END = 204;

    int GAME_JOIN_TIMEOUT = 305;
    int GAME_OVERTIME = 306;

    int DISCHARGE = 500;

    int type();
    void onMessage(InboundMessage pendingInboundMessage);
    void relay();
}
