package com.tarantula.platform.room;

import com.tarantula.cci.udp.UDPChannel;
import com.tarantula.game.GameZone;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class GameZoneIndex {

    public GameZone gameZone;
    public AtomicInteger maxRoomPoolSize;

    //dedicated settings
    public LinkedBlockingDeque<ConnectionStub>  pendingConnections;
    public ArrayBlockingQueue<UDPChannel> pendingPushChannels;
    public GameRoom gameRoom;

    public ArrayBlockingQueue<GameRoom> pendingRooms;
    public LinkedBlockingDeque<GameRoom> runningRooms;

}
