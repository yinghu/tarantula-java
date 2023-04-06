package com.tarantula.platform.room;

import com.icodesoftware.protocol.Channel;

import java.util.concurrent.LinkedBlockingDeque;

public class RemoteGameServer {

    public ConnectionStub connectionStub;

    public LinkedBlockingDeque<Channel> runningChannels;


}
