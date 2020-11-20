package com.icodesoftware.integration;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.integration.server.push.ServerPushMessageHandler;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.InboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class DischargeMessageHandler extends AbstractMessageHandler {

    private static TarantulaLogger log = JDKLogger.getLogger(ServerPushMessageHandler.class);

    public DischargeMessageHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }
    @Override
    public int type() {
        return DISCHARGE;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        log.warn("message id/type ->["+pendingInboundMessage.messageId()+"]["+pendingInboundMessage.type()+"] from ["+pendingInboundMessage.source()+"] discharged");
    }
}
