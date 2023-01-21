package com.tarantula.platform.room;

import com.icodesoftware.service.ClusterProvider;
import com.tarantula.game.GameZone;
import com.tarantula.platform.IndexSet;

import java.util.concurrent.ArrayBlockingQueue;

public class GameZoneIndex {
    public GameZone gameZone;
    public IndexSet roomIndex;
    public ClusterProvider.ClusterStore roomStore;
    //no distributed game rooms
    public ArrayBlockingQueue<String> pendingRooms;
}
