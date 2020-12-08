package com.icodesoftware.protocol;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public interface MessageHandler {

    int ACK = 0;
    int JOIN = 1;
    int MOVE = 2;
    int LEAVE = 3;
    int SPAWN = 4;
    int PING = 5;
    int PONG = 6;
    int COLLISION = 7;
    int SYNC = 8;
    int ACTION = 9;
    int DESTROY = 10;

    int ON_JOINED = 100;
    int ON_LEFT = 101;
    int ON_KICKED_OFF = 102;
    int ON_ACTION = 103;
    int ON_SYNC = 104;

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
