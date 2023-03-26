package com.tarantula.game.blackjack;

import com.icodesoftware.Channel;
import com.icodesoftware.Room;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.*;
import com.icodesoftware.util.ScheduleRunner;

public class BlackjackModule implements GameModule{

    private GameContext gameContext;
    private BlackjackGame blackjackGame;
    private Room room;
    public void setup(Room room, GameContext gameContext){
        this.room = room;
        this.gameContext = gameContext;
        this.blackjackGame = new BlackjackGame(3,true,true);
        this.gameContext.schedule(new ScheduleRunner(5000,()->{
            System.out.println("MAX>>"+room.capacity());
        }));
    }


    @Override
    public byte[] onRequest(Session session,MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
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
        messageBuffer.writeShort((short)10);
        messageBuffer.writeFloat(100);
        messageBuffer.writeUTF8("running");
        messageBuffer.flip();
        messageBuffer.readHeader();
        callback.onRelay(messageHeader,messageBuffer);
    }

    @Override
    public void onJoined(Channel channel) {
        System.out.println("JOINED->"+channel.owner());
    }

    @Override
    public void onLeft(Channel channel) {
        System.out.println("LEFT->"+channel.owner());
    }
}
