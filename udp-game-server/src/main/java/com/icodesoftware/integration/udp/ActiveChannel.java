package com.icodesoftware.integration.udp;

import com.icodesoftware.protocol.ChannelHeader;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.protocol.UserChannel;

public class ActiveChannel extends ChannelHeader {

    public ActiveChannel(String owner,int channelId,int sessionId){
        this.owner = owner;
        this.channelId = channelId;
        this.sessionId = sessionId;
    }

    public ActiveChannel(int sessionId){
        this.sessionId = sessionId;
    }

    public void register(UserChannel userChannel, UDPEndpointServiceProvider.CipherListener cipherListener){
        this.messageBuffer = new MessageBuffer();
        this.userChannel = userChannel;
        this.cipherListener = cipherListener;
    }
}
