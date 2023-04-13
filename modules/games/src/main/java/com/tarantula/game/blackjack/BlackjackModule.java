package com.tarantula.game.blackjack;

import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.tarantula.game.Card;
import java.util.concurrent.ConcurrentHashMap;

public class BlackjackModule implements GameModule{

    private GameContext gameContext;
    private BlackjackGame blackjackGame;
    private Room room;
    private RoomListener roomListener;
    private ConcurrentHashMap<Integer,Channel> channels;

    public void setup(Room room, GameContext gameContext){
        this.room = room;
        this.gameContext = gameContext;
        this.blackjackGame = new BlackjackGame(3,true,true);
        this.channels = new ConcurrentHashMap<>();
        this.gameContext.log("Blackjack module started->"+room,OnLog.WARN);
    }
    public Room room(){
        return this.room;
    }

    @Override
    public byte[] onRequest(Session session,MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        short cmd = messageBuffer.readShort();
        GameServiceProxy messageListener = gameContext.gameServiceProxy(cmd);
        return messageListener.onService(session,messageHeader,messageBuffer);
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
    }

    @Override
    public void onValidated(Channel channel) {
        this.channels.put(channel.sessionId(),channel);
    }

    @Override
    public void onJoined(Channel channel) {
        if(!this.channels.containsKey(channel.sessionId())) return;
        if(room.totalJoined() == room.joinsOnStart()){
            roomListener.onStarted(this.room);
        }
    }

    @Override
    public void onLeft(Channel channel) {
        Channel removed = channels.remove(channel.sessionId());
        if(removed==null) return;
        roomListener.onUpdated(this.room,"{}".getBytes());
        if(room.totalLeft() == room.capacity()) {
            roomListener.onEnded(this.room);
        }
    }

    public void registerRoomListener(RoomListener roomListener){
        this.roomListener = roomListener;
    }

    public void close(){
        this.gameContext.log("close game->",OnLog.WARN);
    }

    public void reset(){
        this.gameContext.log("reset game->",OnLog.WARN);
    }

    public void update(GameServiceProvider gameServiceProvider,byte[] payload){
        this.gameContext.log("Updated game->",OnLog.WARN);
    }
}
