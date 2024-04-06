package com.icodesoftware.integration.udp;

import com.icodesoftware.protocol.*;

public class ActiveChannel extends ChannelHeader {

    public ActiveChannel(String owner,long stub,int channelId,int sessionId){
        this.channelId = channelId;
        this.sessionId = sessionId;
        this.stub = new ActiveSession(owner,stub);
    }


    public void register(UserChannel userChannel, UDPEndpointServiceProvider.CipherListener cipherListener){
        this.messageBuffer = new MessageBuffer();
        this.userChannel = userChannel;
        this.cipherListener = cipherListener;
    }


    void onRequest(MessageBuffer.MessageHeader header,byte[] payload){
        if(payload==null || payload.length==0) return;
        header.commandId = Messenger.ON_REQUEST;
        super.onBatch(header,payload);
    }
}
