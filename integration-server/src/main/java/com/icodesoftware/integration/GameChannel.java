package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 10/16/2020.
 */
public interface GameChannel {
    long channelId();
    void onMessage(InboundMessage pendingInboundMessage);

    void join(int sessionId, SocketAddress socketAddress);
    void leave(int sessionId, SocketAddress socketAddress);
    void relay(int messageId,boolean ack,MessageHandler messageHandler,OutboundMessage pendingOutboundMessage);

    void ack(int sessionId,int messageId,SocketAddress source);
    void ack(int sessionId,int messageId);

    void ping();
    void pong(int sessionId);

    void retry();
    void pending(int sessionId, int messageId, ByteBuffer pending, MessageHandler callback);
}
