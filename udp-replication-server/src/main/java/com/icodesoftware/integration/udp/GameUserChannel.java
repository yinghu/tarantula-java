package com.icodesoftware.integration.udp;

import com.icodesoftware.protocol.*;

public class GameUserChannel extends UserChannel {

    private UDPEndpointServiceProvider.UserSessionValidator userSessionValidator;
    private UDPEndpointServiceProvider.SessionListener sessionListener;

    public GameUserChannel(int channelId, Messenger messenger, UDPEndpointServiceProvider.UserSessionValidator userSessionValidator, UDPEndpointServiceProvider.SessionListener sessionListener){
        super(channelId,messenger);
        this.userSessionValidator = userSessionValidator;
        this.sessionListener = sessionListener;
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

    @Override
    protected boolean validate(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        return userSessionValidator.validate(messageHeader,messageBuffer);
    }

    @Override
    protected void onTimeout(int channelId,int sessionId){
        this.sessionListener.onTimeout(channelId,sessionId);
    }
}
