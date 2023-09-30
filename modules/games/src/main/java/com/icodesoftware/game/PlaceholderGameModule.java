package com.icodesoftware.game;

import com.icodesoftware.*;
import com.icodesoftware.protocol.*;

public class PlaceholderGameModule extends GameModuleHeader {

    @Override
    public byte[] onRequest(Session session, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        return gameContext.gameServiceProvider().onRequest(session,messageHeader,messageBuffer);
    }

    @Override
    public void onAction(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback) {
        gameContext.gameServiceProvider().onAction(messageHeader,messageBuffer,callback);
    }

    public void close(){
        this.gameContext.log("room close",OnLog.WARN);
    }

    public void reset(){
        this.gameContext.log("room reset",OnLog.WARN);
    }

    @Override
    public void countdown(long durationCountdown) {
        //this.gameContext.log("Remaining time->"+durationCountdown,OnLog.WARN);
        if(!closed && durationCountdown <= room.overtime() ){
            closed = true;
            this.roomListener.onClosed(room);
        }
        MessageBuffer.MessageHeader h = new MessageBuffer.MessageHeader();
        h.commandId = Messenger.ON_PUSH;
        h.channelId = room().channelId();
        playerUpdates.forEach((k,p)->p.channel.write(h,"{}".getBytes()));
    }
}
