package com.icodesoftware.integration;

import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;
import com.icodesoftware.service.Serviceable;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 10/16/2020.
 */
public interface GameChannelService extends Serviceable {

    boolean validateTicket(int stub,String login,String ticket);
    int sessionId();
    int messageId();
    int[] messageIdRange();
    GameChannel gameChannel(long connectionId);
    MessageHandler messageHandler(int type);

    byte[] encode(OutboundMessage outboundMessage);

    void pendingOutbound(byte[] outboundMessage,SocketAddress source);

    void onUpdate(Game game,String type,byte[] payload);
}
