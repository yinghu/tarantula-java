package com.tarantula.platform.room;


import com.icodesoftware.protocol.GameModule;
import com.icodesoftware.service.ClusterProvider;
import com.tarantula.cci.udp.UDPChannel;
import com.tarantula.game.GameZone;
import com.tarantula.platform.IndexSet;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class GameZoneIndex {

    public GameZone gameZone;
    public IndexSet roomIndex;
    public AtomicInteger maxRoomPoolSize;

    //distributed game rooms
    public ClusterProvider.ClusterStore roomStore;

    //no distributed game rooms
    public ArrayBlockingQueue<String> pendingRooms;

    // push channels
    public ArrayBlockingQueue<UDPChannel> pendingChannels;

    // use as shared if dedicated = true
    public GameRoom gameRoom;

}
