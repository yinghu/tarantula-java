package com.icodesoftware.protocol;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public interface MessageHandler {

    int ACK = 0;
    int JOIN = 1;
    int LEAVE = 2;
    int PING = 3;
    int PONG = 4;
    int SYNC = 5;
    int MOVE = 6;
    int SPAWN = 7;
    int COLLISION = 8;
    int DESTROY = 9;

    int ACTION = 10;

    int ON_JOINED = 100;
    int ON_LEFT = 101;
    int ON_KICKED_OFF = 102;
    int ON_ACTION = 103;
    int ON_SYNC = 104;
    int ON_COLLISION = 105;

    int SERVER_PUSH = 200;
    int GAME_SPEC = 201;
    int GAME_START = 202;
    int GAME_CLOSING = 203;
    int GAME_CLOSE = 204;
    int GAME_END = 205;
    int GAME_UPDATE = 206;

    int GAME_JOIN_TIMEOUT = 305;
    int GAME_OVERTIME = 306;

    int DISCHARGE = 500;

    int type();
    void onMessage(InboundMessage pendingInboundMessage);
    void relay();
}
