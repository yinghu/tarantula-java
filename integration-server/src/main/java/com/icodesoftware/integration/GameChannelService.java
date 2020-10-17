package com.icodesoftware.integration;

import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.PendingOutboundMessage;
import com.icodesoftware.service.Serviceable;

import java.net.SocketAddress;

/**
 * Created by yinghu lu on 10/16/2020.
 */
public interface GameChannelService extends Serviceable {
    boolean validateTicket(byte[] payload);
    int sessionId();
    GameChannel gameChannel(long connectionId);
    MessageHandler messageHandler(int type);
    boolean send(PendingOutboundMessage outboundMessage, SocketAddress source);
}
