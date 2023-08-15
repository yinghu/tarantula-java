package com.tarantula.platform.event;


import com.icodesoftware.service.OnReplication;

import com.tarantula.platform.service.persistence.ScopedOnReplication;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

abstract public class OnReplicationEvent extends Data implements EventOnReplication {

    protected OnReplication[] data;

    private ArrayBlockingQueue<ScopedOnReplication> pendingQueue;

    public OnReplicationEvent(){}
    public OnReplicationEvent(int pendingSize, String sourceNode, String destination){
        this.source = sourceNode;
        this.destination = destination;
        this.pendingQueue = new ArrayBlockingQueue<>(pendingSize);
    }

    @Override
    public String toString(){
        return "on replication event ->"+destination;
    }
    public boolean offer(ScopedOnReplication scopedOnReplication){
        return pendingQueue.offer(scopedOnReplication);
    }
    public void drain(){
        ArrayList<ScopedOnReplication> list = new ArrayList<>();
        int drained = pendingQueue.drainTo(list);
        data = new OnReplication[drained];
        for (int i=0; i< drained;i++){
            data[i] = list.get(i).read();
        }
    }
    public void drop(){
        ArrayList<ScopedOnReplication> list = new ArrayList<>();
        pendingQueue.drainTo(list);
        list.forEach(e->e.drop());
    }
    public OnReplication[] data(){
        return data;
    }
}
