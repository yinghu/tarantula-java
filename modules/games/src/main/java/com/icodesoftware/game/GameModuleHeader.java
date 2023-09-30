package com.icodesoftware.game;

import com.icodesoftware.Room;
import com.icodesoftware.RoomListener;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.*;

import java.util.concurrent.ConcurrentHashMap;

public class GameModuleHeader implements GameModule {
    protected GameContext gameContext;

    protected Room room;
    protected RoomListener roomListener;

    protected ConcurrentHashMap<Integer, PlayerUpdate> playerUpdates;

    protected boolean closed;
    @Override
    public void onValidated(Channel channel) {
        playerUpdates.put(channel.sessionId(),new PlayerUpdate(channel));
    }

    @Override
    public void onJoined(Channel channel) {
        if(!playerUpdates.containsKey(channel.sessionId())) return;
        if(room.totalJoined() == room.joinsOnStart()){
            roomListener.onStarted(room);
        }
    }

    @Override
    public void onLeft(Channel channel) {
        PlayerUpdate removed = playerUpdates.remove(channel.sessionId());
        if(removed==null) return;
        removed.toBatch();
        UpdateBatch batch = new UpdateBatch(new PlayerUpdate[]{removed});
        this.roomListener.onUpdated(room,batch.toBytes());
        if(room.totalLeft() == room.capacity()){
            this.roomListener.onEnded(this.room);
        }
    }

    @Override
    public void setup(Room room, GameContext gameContext) {
        this.room = room;
        this.gameContext = gameContext;
        this.playerUpdates = new ConcurrentHashMap<>();
        this.closed = false;
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
        //UpdateBatch updateBatch = UpdateBatch.fromBytes(payload);
        //for(PlayerUpdate playerUpdate :updateBatch.playerUpdates){
            //for(GameExperience gameExperience : playerUpdate.gameExperiences){
                //gameServiceProvider.updateStatistics(room,playerUpdate.systemId,playerUpdate.stub,gameExperience.name,gameExperience.statisticsDelta);
                //gameServiceProvider.updateExperience(room,playerUpdate.systemId,playerUpdate.stub,gameExperience.experienceDelta);
            //}
        //}
    }

    public void countdown(long durationCountdown){

    }
}
