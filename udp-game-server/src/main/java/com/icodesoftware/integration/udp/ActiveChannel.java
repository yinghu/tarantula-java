package com.icodesoftware.integration.udp;

import com.icodesoftware.protocol.ChannelHeader;

public class ActiveChannel extends ChannelHeader {

    public ActiveChannel(String owner,int sessionId){
        this.owner = owner;
        this.sessionId = sessionId;
    }

    public ActiveChannel(int sessionId){
        this.sessionId = sessionId;
    }
}
