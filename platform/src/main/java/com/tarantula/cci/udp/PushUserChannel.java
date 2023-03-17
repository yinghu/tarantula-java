package com.tarantula.cci.udp;

import com.icodesoftware.protocol.*;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;

public class PushUserChannel extends UserChannel {

    private UDPEndpointServiceProvider.RequestListener requestListener;
    private UDPEndpointServiceProvider.UserSessionValidator userSessionValidator;
    private UDPEndpointServiceProvider.SessionListener sessionListener;
    private UDPEndpointServiceProvider.ActionListener actionListener;
    private UDPEndpointServiceProvider.CipherListener cipherListener;

    public PushUserChannel(int channelId, Messenger messenger, UDPEndpointServiceProvider.CipherListener cipherListener, UDPEndpointServiceProvider.UserSessionValidator userSessionValidator, UDPEndpointServiceProvider.SessionListener sessionListener, UDPEndpointServiceProvider.RequestListener requestListener, UDPEndpointServiceProvider.ActionListener actionListener){
        super(channelId,messenger);
        this.cipherListener = cipherListener;
        this.requestListener = requestListener;
        this.userSessionValidator = userSessionValidator;
        this.sessionListener = sessionListener;
        this.actionListener = actionListener;
    }

    @Override
    protected void onRelay(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer) {
        if(messageHeader.encrypted && !cipherListener.encrypt(messageHeader,messageBuffer)) return;
        super.onRelay(messageHeader,messageBuffer);
    }

    @Override
    protected void onAction(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer) {
        actionListener.onAction(messageHeader,messageBuffer,(h,m)->{
            this.onRelay(h,m);
        });
    }

    @Override
    protected void onJoin(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        messageHeader.ack = true;
        messageHeader.encrypted = true;
        messageHeader.commandId = Messenger.ON_JOIN;
        messageHeader.sequence = sequence.incrementAndGet();
        messageBuffer.reset();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.writeInt(messageHeader.sessionId);
        messageBuffer.writeLong(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        messageBuffer.flip();
        messageBuffer.readHeader();
        if(!cipherListener.encrypt(messageHeader,messageBuffer)) return;
        messageBuffer.rewind();
        byte[] buffer = messenger.buffer();
        int length = messageBuffer.toArray(buffer);
        PendingAckMessage pendingAckMessage = new PendingAckMessage(messageHeader.sessionId,buffer,length,false);
        UserSession userSession = userSessionIndex.get(messageHeader.sessionId);
        messenger.queue(pendingAckMessage.buffer,pendingAckMessage.length,userSession.source);
        pendingAckMessage.pendingAck= 1;
        pendingAckMessageIndex.put(messageHeader,pendingAckMessage);
    }

    @Override
    protected void onLeave(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        userSessionIndex.remove(messageHeader.sessionId);
        sessionListener.onTimeout(channelId(),messageHeader.sessionId);
    }

    @Override
    protected void onRequest(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        requestListener.onRequest(messageHeader,messageBuffer);
    }

    @Override
    protected boolean validate(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        return userSessionValidator.validate(messageHeader,messageBuffer);
    }

    @Override
    protected void onTimeout(int channelId,int sessionId){
        this.sessionListener.onTimeout(channelId,sessionId);
    }
    @Override
    protected void onPing(){
        //super.onPing();
    }

}
