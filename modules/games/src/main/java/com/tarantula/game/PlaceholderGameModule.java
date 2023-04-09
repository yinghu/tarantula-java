package com.tarantula.game;

import com.icodesoftware.OnLog;
import com.icodesoftware.Room;
import com.icodesoftware.RoomListener;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.ServiceContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PlaceholderGameModule implements GameModule {

    private GameContext gameContext;
    private Room room;
    private RoomListener roomListener;

    private AtomicInteger totalJoined;

    private ConcurrentHashMap<Integer,Channel> channels;
    @Override
    public void onValidated(Channel channel) {
        channels.put(channel.sessionId(),channel);
    }

    @Override
    public void onJoined(Channel channel) {
        if(!channels.containsKey(channel.sessionId())) return;
        if(totalJoined.incrementAndGet()==room.joinsOnStart()){
            roomListener.onStarted(room);
        }
    }

    @Override
    public void onLeft(Channel channel) {
        if(channels.remove(channel.sessionId())==null) return;
        if(totalJoined.decrementAndGet()>0) return;
        this.roomListener.onUpdated(room,"".getBytes());
        this.roomListener.onEnded(this.room);
    }

    @Override
    public void setup(Room room, GameContext gameContext) {
        this.room = room;
        this.gameContext = gameContext;
        this.channels = new ConcurrentHashMap<>();
        this.totalJoined = new AtomicInteger(0);
        this.gameContext.log("Placeholder game  module started on channel ["+room.channelId()+"]", OnLog.WARN);
    }

    @Override
    public Room room() {
        return this.room;
    }

    @Override
    public void registerRoomListener(RoomListener roomListener) {
        this.roomListener = roomListener;
    }

    @Override
    public byte[] onRequest(Session session, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        if(room.dedicated()) return null;
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
    }

    public void close(){

    }

    public void reset(){

    }

    public void update(ServiceContext serviceContext, byte[] payload){
        this.gameContext.log("update room",OnLog.WARN);
    }
}
