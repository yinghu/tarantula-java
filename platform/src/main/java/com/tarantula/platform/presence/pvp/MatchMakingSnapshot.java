package com.tarantula.platform.presence.pvp;


import com.icodesoftware.util.IntegerKey;

import java.util.concurrent.ArrayBlockingQueue;

public class MatchMakingSnapshot {

    public final IntegerKey key;
    public final ArrayBlockingQueue<DefenseTeamIndex> pending;

    public MatchMakingSnapshot(IntegerKey key,int pendingSize){
        this.key = key;
        this.pending = new ArrayBlockingQueue<>(pendingSize);
    }
}
