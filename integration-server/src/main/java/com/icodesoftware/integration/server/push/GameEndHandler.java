package com.icodesoftware.integration.server.push;

import com.icodesoftware.integration.AbstractMessageHandler;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.OutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class GameEndHandler extends AbstractMessageHandler {

    public GameEndHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public int type() {
        return GAME_END;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        connectionId = pendingInboundMessage.connectionId();
        messageId = pendingInboundMessage.messageId();
        ack = true;
        outboundMessage = new OutboundMessage();
        outboundMessage.type(GAME_END);
        outboundMessage.sequence(0);
        outboundMessage.messageId(messageId);
        outboundMessage.ack(ack);
        this.gameChannelService.gameChannel(pendingInboundMessage.connectionId()).onGame().onAction(pendingInboundMessage);
    }
}
