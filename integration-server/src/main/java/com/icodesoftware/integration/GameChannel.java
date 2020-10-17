package com.icodesoftware.integration;

import com.icodesoftware.protocol.PendingInboundMessage;

/**
 * Created by yinghu lu on 10/16/2020.
 */
public interface GameChannel {
    long channelId();
    void onMessage(PendingInboundMessage pendingInboundMessage);

}
