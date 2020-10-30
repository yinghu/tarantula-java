package com.icodesoftware.integration;

import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;
import com.icodesoftware.protocol.PendingMessage;
import com.icodesoftware.service.Serviceable;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 10/16/2020.
 */
public interface GameChannelService extends Serviceable {
    boolean validateTicket(byte[] payload);
    int sessionId();
    int messageId();
    int[] messageIdRange();
    GameChannel gameChannel(long connectionId);
    MessageHandler messageHandler(int type);
    ByteBuffer pendingMessage(PendingMessage pendingMessage);

    ByteBuffer pendingMessage(OutboundMessage outboundMessage, SocketAddress source);
    //boolean retry(ByteBuffer pendingMessage,SocketAddress source);
}
