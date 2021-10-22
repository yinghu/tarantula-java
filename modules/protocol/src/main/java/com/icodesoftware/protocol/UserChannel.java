package com.icodesoftware.protocol;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class UserChannel {

    public int channelId;
    public ConcurrentHashMap<Integer,UserSession> userSessionIndex;
    public Messenger messenger;
    public UserChannel(int channelId,Messenger messenger){
        this.channelId = channelId;
        this.messenger = messenger;
        this.userSessionIndex = new ConcurrentHashMap<>();
    }

    public void onMessage(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer, SocketAddress source){
        userSessionIndex.put(messageHeader.sessionId,new UserSession(messageHeader.sessionId,source));

        if(messageHeader.ack){

            //messenger.send(messageBuffer,source);
        }
        userSessionIndex.forEach((sid,session)->{
            if(messageHeader.sessionId!=sid) messenger.send(messageBuffer,session.source);
        });
    }

}
