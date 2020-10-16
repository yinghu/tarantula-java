package com.icodesoftware.integration;

import com.icodesoftware.integration.udp.UDPService;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.PayloadBuffer;
import com.icodesoftware.protocol.PendingInboundMessage;
import com.icodesoftware.protocol.PendingOutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class JoinMessageHandler implements MessageHandler {
    private final UDPService udpService;
    public JoinMessageHandler(UDPService udpService){
        this.udpService = udpService;
    }
    @Override
    public int type() {
        return 0;
    }

    @Override
    public void onMessage(PendingInboundMessage pendingInboundMessage) {
        PendingOutboundMessage pendingOutboundMessage = new PendingOutboundMessage();
        pendingOutboundMessage.ack(true);
        pendingOutboundMessage.timestamp(pendingInboundMessage.timestamp());
        pendingOutboundMessage.messageId(pendingInboundMessage.messageId());
        pendingOutboundMessage.type(pendingInboundMessage.type());
        pendingOutboundMessage.sequence(pendingInboundMessage.sequence());
        this.udpService.validateTicket(pendingInboundMessage.payload());
        pendingOutboundMessage.payload("hello".getBytes());
        this.udpService.send(pendingOutboundMessage,pendingInboundMessage.source());
    }
}
