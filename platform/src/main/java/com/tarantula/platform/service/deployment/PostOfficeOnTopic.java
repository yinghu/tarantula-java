package com.tarantula.platform.service.deployment;

import com.icodesoftware.PostOffice;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.EventService;
import com.tarantula.platform.event.TopicMapStoreSyncEvent;

public class PostOfficeOnTopic implements PostOffice.OnTopic {

    private final String topic;
    private final EventService eventService;

    public PostOfficeOnTopic(String topic,EventService eventService){
        this.topic = topic;
        this.eventService = eventService;
    }

    @Override
    public void send(Recoverable data) {
        TopicMapStoreSyncEvent event = new TopicMapStoreSyncEvent(topic,data.getFactoryId(),data.getClassId(),data.key().asString(),data.toBinary());
        eventService.publish(event);
    }

    @Override
    public void send(byte[] data) {
        TopicMapStoreSyncEvent event = new TopicMapStoreSyncEvent(topic,data);
        eventService.publish(event);
    }
}
