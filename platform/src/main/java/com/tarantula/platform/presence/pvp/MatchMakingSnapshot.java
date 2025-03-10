package com.tarantula.platform.presence.pvp;


import java.util.concurrent.ArrayBlockingQueue;

public class MatchMakingSnapshot {

    public final ArrayBlockingQueue<Long> pending;

    public MatchMakingSnapshot(int pendingSize){

        this.pending = new ArrayBlockingQueue<>(pendingSize);
    }
}
