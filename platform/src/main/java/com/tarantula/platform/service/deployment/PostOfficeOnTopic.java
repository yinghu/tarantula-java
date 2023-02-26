package com.tarantula.platform.service.deployment;

import com.icodesoftware.PostOffice;
import com.icodesoftware.Session;
import com.icodesoftware.service.EventService;
import com.tarantula.platform.event.ServerPushEvent;

public class PostOfficeOnTopic implements PostOffice.OnTopic {

    private final String topic;
    private final EventService eventService;

    public PostOfficeOnTopic(String topic,EventService eventService){
        this.topic = topic;
        this.eventService = eventService;
    }

    public void send(String trackId,byte[] data){
        ServerPushEvent serverPushEvent = new ServerPushEvent(topic,trackId,data);
        eventService.publish(serverPushEvent);
    }
    @Override
    public void send(String trackId,Session.Header header,byte[] data) {
        ServerPushEvent serverPushEvent = new ServerPushEvent(topic,trackId,header,data);
        eventService.publish(serverPushEvent);
    }
}
