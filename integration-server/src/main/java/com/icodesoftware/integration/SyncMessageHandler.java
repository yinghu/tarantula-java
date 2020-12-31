package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.OutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class SyncMessageHandler extends AbstractMessageHandler {

    public SyncMessageHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public int type() {
        return SYNC;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        GameChannel _gameChannel = gameChannelService.gameChannel(pendingInboundMessage.connectionId());
        if(!_gameChannel.onGame().onSync(pendingInboundMessage)){
            return;
        }
        OutboundMessage pendingOutboundMessage = new OutboundMessage();
        pendingOutboundMessage.ack(pendingInboundMessage.ack());
        int mid = this.gameChannelService.messageId();
        pendingOutboundMessage.messageId(mid);
        pendingOutboundMessage.sessionId(pendingInboundMessage.sessionId());
        pendingOutboundMessage.type(pendingInboundMessage.type());
        pendingOutboundMessage.sequence(pendingInboundMessage.sequence());
        pendingOutboundMessage.payload(pendingInboundMessage.payload());
        _gameChannel.relay(mid,pendingInboundMessage.ack(),null,pendingOutboundMessage);
    }
}
