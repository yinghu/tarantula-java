package com.tarantula.platform.room;

import com.icodesoftware.OnLog;
import com.icodesoftware.Room;
import com.icodesoftware.RoomListener;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.*;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DedicatedGameModuleProxy implements GameModule {

    private GameModule gameModule;

    private GameContext gameContext;
    private Room room;
    private RoomListener roomListener;

    private AtomicInteger totalJoined;

    private ConcurrentHashMap<Integer,Channel> channels;

    public DedicatedGameModuleProxy(GameModule gameModule){
        this.gameModule = gameModule;
    }
    @Override
    public void onValidated(Channel channel) {
        channels.put(channel.sessionId(),channel);
    }

    @Override
    public void onJoined(Channel channel) {
        if(!channels.containsKey(channel.sessionId())) return;
        totalJoined.incrementAndGet();
    }

    @Override
    public void onLeft(Channel channel) {
        if(channels.remove(channel.sessionId())==null) return;
        totalJoined.decrementAndGet();
    }

    @Override
    public void setup(Room room, GameContext gameContext) {
        this.room = room;
        this.gameContext = gameContext;
        this.channels = new ConcurrentHashMap<>();
        this.totalJoined = new AtomicInteger(0);
        this.gameContext.log("Single dedicated game  module started on game zone ["+room.owner()+"]", OnLog.WARN);
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
        if(!channels.containsKey(messageHeader.sessionId)){
            this.gameContext.log("Invalid session ["+messageHeader.sessionId+"]",OnLog.WARN);
            return null;
        }
        short cmd = messageBuffer.readShort();
        GameServiceProxy messageListener = gameContext.gameServiceProxy(cmd);
        return messageListener.onService(session,messageHeader,messageBuffer);
    }

    @Override
    public void onAction(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback) {

    }

    public void close(){
    }

    public void reset(){

    }
    public void update(GameServiceProvider gameServiceProvider,byte[] payload){
        //this.gameContext.log("Update room",OnLog.WARN);
        this.gameModule.update(gameServiceProvider,payload);
    }
}
