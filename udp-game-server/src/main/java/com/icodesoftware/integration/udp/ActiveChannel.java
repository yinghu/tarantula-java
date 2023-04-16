package com.icodesoftware.integration.udp;

import com.icodesoftware.protocol.ChannelHeader;

public class ActiveChannel extends ChannelHeader {

    public ActiveChannel(String owner,int channelId,int sessionId){
        this.owner = owner;
        this.channelId = channelId;
        this.sessionId = sessionId;
    }

    public ActiveChannel(int sessionId){
        this.sessionId = sessionId;
    }
}
