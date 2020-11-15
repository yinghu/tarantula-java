package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;

/**
 * Created by yinghu lu on 11/15/2020.
 */
public interface Game {

    String zoneId();
    String roomId();
    void onMessage(InboundMessage inboundMessage);
}
