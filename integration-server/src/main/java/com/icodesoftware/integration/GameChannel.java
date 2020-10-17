package com.icodesoftware.integration;

import com.icodesoftware.protocol.PendingInboundMessage;

import java.net.SocketAddress;

/**
 * Created by yinghu lu on 10/16/2020.
 */
public interface GameChannel {
    long channelId();
    void onMessage(PendingInboundMessage pendingInboundMessage);
    void join(int sessionId, SocketAddress socketAddress);
    void leave(int sessionId, SocketAddress socketAddress);
}
