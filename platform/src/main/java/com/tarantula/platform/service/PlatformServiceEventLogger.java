package com.tarantula.platform.service;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.ServiceEvent;
import com.icodesoftware.service.ServiceEventLogger;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class PlatformServiceEventLogger implements ServiceEventLogger {

    private DataStore dataStore;

    private ArrayBlockingQueue<ServiceEvent> pendingEvents;


    public PlatformServiceEventLogger(DataStore dataStore,int maxPendingEvents){
        this.dataStore = dataStore;
        this.pendingEvents = new ArrayBlockingQueue<>(maxPendingEvents);
    }
    @Override
    public void log(ServiceEvent event) {
        pendingEvents.add(event);
    }

    @Override
    public void flush(){
        ArrayList<ServiceEvent> pending = new ArrayList<>();
        pendingEvents.drainTo(pending);
        pending.forEach(e->dataStore.create(e));
    }
}