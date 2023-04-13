package com.tarantula.game.blackjack;

import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.tarantula.game.Card;
import com.tarantula.game.GameModuleHeader;
import com.tarantula.game.PlayerUpdate;


public class BlackjackModule extends GameModuleHeader {


    private BlackjackGame blackjackGame;

    @Override
    public void setup(Room room, GameContext gameContext){
        super.setup(room,gameContext);
        this.blackjackGame = new BlackjackGame(3,true,true);
        this.gameContext.log("Blackjack module started->"+room,OnLog.WARN);
    }


    @Override
    public void onAction(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback) {
        Card[] hand = blackjackGame.deal();
        messageHeader.ack = true;
        messageHeader.encrypted = true;
        messageHeader.commandId = Messenger.ON_ACTION;
        messageBuffer.reset();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.writeInt(hand[0].suit.ordinal());
        messageBuffer.writeUTF8(hand[0].name);
        messageBuffer.writeInt(hand[0].rank);
        messageBuffer.writeInt(hand[1].suit.ordinal());
        messageBuffer.writeUTF8(hand[1].name);
        messageBuffer.writeInt(hand[1].rank);
        messageBuffer.flip();
        messageBuffer.readHeader();
        callback.onRelay(messageHeader,messageBuffer);
        PlayerUpdate playerUpdate = playerUpdates.get(messageHeader.sessionId);
        if(playerUpdate==null) return;
        playerUpdate.update("bjs",1,10);
    }


    public void close(){
        this.gameContext.log("close game->",OnLog.WARN);
    }

    public void reset(){
        this.gameContext.log("reset game->",OnLog.WARN);
    }

}
