package com.icodesoftware.integration.server.push;

import com.icodesoftware.integration.AbstractMessageHandler;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.OutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class GameJoinTimeoutHandler extends AbstractMessageHandler {

    public GameJoinTimeoutHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public int type() {
        return GAME_JOIN_TIMEOUT;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        gameChannelService.gameChannel(pendingInboundMessage.connectionId()).onGame().onJoinTimeout();
        connectionId = pendingInboundMessage.connectionId();
        messageId = pendingInboundMessage.messageId();
        ack = true;
        outboundMessage = new OutboundMessage();
        outboundMessage.type(GAME_JOIN_TIMEOUT);
        outboundMessage.sequence(0);
        outboundMessage.messageId(messageId);
        outboundMessage.ack(ack);
    }
}
