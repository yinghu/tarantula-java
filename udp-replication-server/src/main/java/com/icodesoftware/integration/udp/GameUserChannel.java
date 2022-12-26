package com.icodesoftware.integration.udp;

import com.icodesoftware.protocol.*;

public class GameUserChannel extends UserChannel {

    public GameUserChannel(int channelId, Messenger messenger, UDPEndpointServiceProvider.UserSessionValidator userSessionValidator, UDPEndpointServiceProvider.SessionListener sessionListener, UDPEndpointServiceProvider.RequestListener requestListener){
        super(channelId,messenger,userSessionValidator,sessionListener,requestListener);
    }

    @Override
    protected void onRelay(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        super.onRelay(messageHeader,messageBuffer);
    }

    @Override
    protected void onJoin(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        super.onJoin(messageHeader,messageBuffer);
    }

    @Override
    protected void onLeave(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        super.onLeave(messageHeader, messageBuffer);
    }
}
