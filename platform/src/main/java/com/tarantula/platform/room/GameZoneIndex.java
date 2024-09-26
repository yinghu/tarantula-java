package com.tarantula.platform.room;

import com.tarantula.game.GameZone;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class GameZoneIndex {

    public GameZone gameZone;

    //dedicated room settings
    public LinkedBlockingDeque<ConnectionStub>  pendingConnections;
    public GameRoom gameRoom;

    //local room settings
    public AtomicInteger[] rooms;
    public ArrayBlockingQueue<RoomStub> pendingRoomStubs;

    public void start(boolean dedicated,int bucketNumber,int maxRoomsPerBucket,int maxConnectionPerNode){
        if(dedicated){
            pendingConnections = new LinkedBlockingDeque<>(maxConnectionPerNode);
            gameRoom = new GameRoomHeader(gameZone,true,0);
            return;
        }
        rooms = new AtomicInteger[bucketNumber];
        for(int i=0;i<bucketNumber;i++){
            rooms[i]= new AtomicInteger(0);
        }
        pendingRoomStubs = new ArrayBlockingQueue<>(bucketNumber*gameZone.capacity()*maxRoomsPerBucket);

    }
}
