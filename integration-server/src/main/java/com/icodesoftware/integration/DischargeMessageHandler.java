package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class DischargeMessageHandler extends AbstractMessageHandler {

    public DischargeMessageHandler(GameChannelService udpService){
        super(udpService);
    }
    @Override
    public int type() {
        return DISCHARGE;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {

    }
}
