package com.tarantula.platform.service.cluster;

import com.icodesoftware.Event;
import com.icodesoftware.service.EventService;
import com.icodesoftware.Closable;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class EventSubscriptionWorker implements Closable,Runnable {

    private final EventService eventService;
    private final ConcurrentLinkedQueue<Event> replicationQueue;
    private final ConcurrentHashMap<String,EventSubscriber> eventSubscribers;
    private boolean running;

    public EventSubscriptionWorker(final EventService eventService,final ConcurrentHashMap<String,EventSubscriber> eventSubscribers, final ConcurrentLinkedQueue<Event> replicationQueue){
        this.eventService = eventService;
        this.eventSubscribers = eventSubscribers;
        this.replicationQueue = replicationQueue;
        this.running = true;
    }

    @Override
    public void close() {
        this.running = false;
    }

    @Override
    public void run() {
        while (running){
            Event pending = this.replicationQueue.poll();
            try{
                if(pending!=null){
                    EventSubscriber eventSubscriber = eventSubscribers.get(pending.destination());
                    if(eventSubscriber!=null){
                        pending.eventService(this.eventService);
                        eventSubscriber.callback.onEvent(pending);
                    }
                }else{
                    Thread.sleep(10);
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
