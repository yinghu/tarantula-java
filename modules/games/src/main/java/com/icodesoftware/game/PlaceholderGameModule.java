package com.icodesoftware.game;

import com.icodesoftware.*;
import com.icodesoftware.protocol.*;

public class PlaceholderGameModule extends GameModuleHeader {


    @Override
    public byte[] onRequest(Session session, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        short cmd = messageBuffer.readShort();
        GameServiceProxy messageListener = gameContext.gameServiceProxy(cmd);
        return messageListener.onService(session,messageHeader,messageBuffer);
    }

    @Override
    public void onAction(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback) {
        messageHeader.ack = true;
        messageHeader.encrypted = true;
        messageHeader.commandId = Messenger.ON_ACTION;
        messageBuffer.reset();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.writeInt(1);
        messageBuffer.writeUTF8("A");
        messageBuffer.writeInt(1);
        messageBuffer.writeInt(2);
        messageBuffer.writeUTF8("B");
        messageBuffer.writeInt(2);
        messageBuffer.flip();
        messageBuffer.readHeader();
        callback.onRelay(messageHeader,messageBuffer);
        PlayerUpdate playerUpdate = playerUpdates.get(messageHeader.sessionId);
        if(playerUpdate==null) return;
        playerUpdate.update("kills",1,10);
        playerUpdate.update("melees",1,10);
        playerUpdate.update("fires",1,3);
        playerUpdate.update("dashes",1,3);
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
