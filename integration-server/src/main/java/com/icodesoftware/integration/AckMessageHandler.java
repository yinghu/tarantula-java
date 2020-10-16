package com.icodesoftware.integration;

import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.PendingInboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class AckMessageHandler implements MessageHandler {
    @Override
    public int type() {
        return ACK;
    }

    @Override
    public void onMessage(PendingInboundMessage pendingInboundMessage) {

    }
}
