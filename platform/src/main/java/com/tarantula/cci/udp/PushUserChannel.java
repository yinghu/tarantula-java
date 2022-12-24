package com.tarantula.cci.udp;

import com.icodesoftware.protocol.*;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;

public class PushUserChannel extends UserChannel {

    public PushUserChannel(int channelId, Messenger messenger, UDPEndpointServiceProvider.UserSessionValidator userSessionValidator, UDPEndpointServiceProvider.SessionListener sessionListener, UDPEndpointServiceProvider.RequestListener requestListener){
        super(channelId,messenger,userSessionValidator,sessionListener,requestListener);
    }

    @Override
    protected void onRelay(MessageBuffer.MessageHeader messageHeader, byte[] payload) {
        //super.onRelay(messageHeader,payload);
    }

    @Override
    protected void onPing(){

    }

    @Override
    protected void onJoin(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        messageBuffer.reset();
        messageHeader.ack = true;
        messageHeader.encrypted = false;
        messageHeader.commandId = Messenger.ON_JOIN;
        messageHeader.sequence = sequence.incrementAndGet();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.writeInt(messageHeader.sessionId);
        messageBuffer.writeLong(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        messageBuffer.flip();
        PendingAckMessage pendingAckMessage = new PendingAckMessage(messageHeader,messageBuffer.toArray());
        UserSession userSession = userSessionIndex.get(messageHeader.sessionId);
        messenger.send(pendingAckMessage.data,pendingAckMessage.length,userSession.source);
        pendingAckMessage.pendingAck=1;
        pendingAckMessageIndex.put(messageHeader.toString(),pendingAckMessage);
    }

    @Override
    protected void onLeave(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        super.onLeave(messageHeader, messageBuffer);
    }

}
