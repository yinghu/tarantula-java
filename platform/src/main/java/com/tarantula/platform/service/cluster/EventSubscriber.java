package com.tarantula.platform.service.cluster;

import com.hazelcast.core.ITopic;
import com.icodesoftware.Event;
import com.icodesoftware.EventListener;

public class EventSubscriber {
    public String registrationKey;
    public ITopic<Event> topic;
    public EventListener callback;
}
