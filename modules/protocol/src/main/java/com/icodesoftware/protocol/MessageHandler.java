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
    int type();
    void onMessage(PendingInboundMessage pendingInboundMessage);
}
