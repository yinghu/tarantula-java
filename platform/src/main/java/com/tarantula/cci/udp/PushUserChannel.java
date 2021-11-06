package com.tarantula.cci.udp;

import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.Messenger;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.protocol.UserChannel;

public class PushUserChannel extends UserChannel {

    public PushUserChannel(int channelId, Messenger messenger, UDPEndpointServiceProvider.UserSessionValidator userSessionValidator, UDPEndpointServiceProvider.SessionListener sessionListener, UDPEndpointServiceProvider.RequestListener requestListener){
        super(channelId,messenger,userSessionValidator,sessionListener,requestListener);
    }

    @Override
    protected void onRelay(MessageBuffer.MessageHeader messageHeader, byte[] payload) {
        //super.onRelay(messageHeader,payload);
    }

    @Override
    protected void onJoin(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        super.onJoin(messageHeader, messageBuffer);
    }

    @Override
    protected void onLeave(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        super.onLeave(messageHeader, messageBuffer);
    }
}
