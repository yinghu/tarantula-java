package com.tarantula.platform.room;

import com.tarantula.game.GameZone;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class GameZoneIndex {

    public GameZone gameZone;
    public AtomicInteger maxRoomPoolSize;

    //dedicated room settings
    public LinkedBlockingDeque<ConnectionStub>  pendingConnections;
    public GameRoom gameRoom;

    //local room settings
    public ArrayBlockingQueue<RoomStub> pendingRoomStubs;

}
