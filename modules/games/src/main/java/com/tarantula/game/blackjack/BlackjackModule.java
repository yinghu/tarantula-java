package com.tarantula.game.blackjack;

import com.icodesoftware.Session;
import com.icodesoftware.protocol.*;

public class BlackjackModule implements GameModule{

    private GameServiceProvider gameServiceProvider;
    private BlackjackGame blackjackGame;

    public void setup(GameServiceProvider gameServiceProvider){
        this.gameServiceProvider = gameServiceProvider;
        this.blackjackGame = new BlackjackGame(3,true,true);
    }


    @Override
    public byte[] onRequest(Session session,MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        short cmd = messageBuffer.readShort();
        GameServiceProxy messageListener = gameServiceProvider.gameServiceProxy(cmd);
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
}
