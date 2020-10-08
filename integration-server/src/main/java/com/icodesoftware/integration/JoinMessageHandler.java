package com.icodesoftware.integration;

import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.PendingInboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class JoinMessageHandler implements MessageHandler {
    @Override
    public int type() {
        return 1;
    }

    @Override
    public void onMessage(PendingInboundMessage pendingInboundMessage) {

    }
}
