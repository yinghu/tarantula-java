package com.tarantula.platform.service.cluster;

import com.hazelcast.core.ITopic;
import com.tarantula.Event;
import com.tarantula.EventListener;

/**
 * Update by yinghu lu on 6/12/2018.
 */
public class EventSubscriber {
    public String registrationKey;
    public ITopic<Event> topic;
    public EventListener callback;
}
