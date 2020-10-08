package com.icodesoftware.protocol;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public interface MessageHandler {
    int type();
    void onMessage(PendingInboundMessage pendingInboundMessage);
}
