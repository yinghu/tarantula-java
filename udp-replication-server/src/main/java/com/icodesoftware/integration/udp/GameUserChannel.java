package com.icodesoftware.integration.udp;

import com.icodesoftware.protocol.*;
import com.icodesoftware.util.BatchUtil;

public class GameUserChannel extends UserChannel {

    private UDPEndpointServiceProvider.UserSessionValidator userSessionValidator;
    private UDPEndpointServiceProvider.SessionListener sessionListener;
    private UDPEndpointServiceProvider.RequestListener requestListener;
    private UDPEndpointServiceProvider.CipherListener cipherListener;

    public GameUserChannel(int channelId, Messenger messenger, UDPEndpointServiceProvider.CipherListener cipherListener,UDPEndpointServiceProvider.UserSessionValidator userSessionValidator, UDPEndpointServiceProvider.SessionListener sessionListener, UDPEndpointServiceProvider.RequestListener requestListener){
        super(channelId,messenger);
        this.cipherListener = cipherListener;
        this.userSessionValidator = userSessionValidator;
        this.sessionListener = sessionListener;
        this.requestListener = requestListener;
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

    @Override
    protected void onPing(){
        //super.onPing();
    }

    @Override
    protected void onRequest(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        byte[] response = requestListener.onMessage(messageHeader,messageBuffer);
        if(response==null) return;
        BatchUtil.Batch batch = BatchUtil.batch(response.length,MessageBuffer.PAYLOAD_SIZE);
        for(BatchUtil.Offset offset : batch.offsets){
            messageBuffer.reset();
            messageHeader.commandId = Messenger.ON_REQUEST;
            messageHeader.encrypted = false;
            messageHeader.batch = offset.batch;
            messageHeader.batchSize = batch.size;
            messageBuffer.writeHeader(messageHeader);
            messageBuffer.writePayload(response,offset.offset,offset.length);
            messageBuffer.flip();
            queue(messageHeader.sessionId,messageBuffer);
        }
    }
}
