package com.icodesoftware.integration.udp;

import com.icodesoftware.protocol.*;

public class GameUserChannel extends UserChannel {

    private UDPEndpointServiceProvider.UserSessionValidator userSessionValidator;

    private UDPEndpointServiceProvider.SessionListener sessionListener;

    private UDPEndpointServiceProvider.CipherListener cipherListener;

    private UDPEndpointServiceProvider.ActionListener actionListener;

    public GameUserChannel(int channelId, Messenger messenger, UDPEndpointServiceProvider.CipherListener cipherListener,UDPEndpointServiceProvider.UserSessionValidator userSessionValidator, UDPEndpointServiceProvider.SessionListener sessionListener, UDPEndpointServiceProvider.ActionListener actionListener){
        super(channelId,messenger);
        this.cipherListener = cipherListener;
        this.userSessionValidator = userSessionValidator;
        this.sessionListener = sessionListener;
        this.actionListener = actionListener;
    }

    @Override
    protected void onRelay(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        if(messageHeader.encrypted && !cipherListener.encrypt(messageHeader,messageBuffer)) return;
        super.onRelay(messageHeader,messageBuffer);
    }

    @Override
    protected void onJoin(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        super.onJoin(messageHeader,messageBuffer);
        sessionListener.onJoined(messageHeader.channelId,messageHeader.sessionId());
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
        this.sessionListener.onLeft(channelId,sessionId);
    }

    @Override
    protected void onPing(){
        //super.onPing();
    }

    @Override
    protected void onAction(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer) {
        actionListener.onAction(messageHeader,messageBuffer,(h,m)->{
            this.onRelay(h,m);
        });
    }


}
