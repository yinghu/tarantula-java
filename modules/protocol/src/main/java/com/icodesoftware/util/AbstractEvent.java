package com.icodesoftware.util;

import com.icodesoftware.Event;
import com.icodesoftware.service.EventService;

public class AbstractEvent extends TROnApplication implements Event {

    protected String tag;
    protected String destination;
    protected int retries;

    protected EventService eventService;
    @Override
    public String tag() {
        return tag;
    }

    @Override
    public void tag(String tag) {
        this.tag = tag;
    }

    @Override
    public String destination() {
        return destination;
    }

    @Override
    public void destination(String destination) {
        this.destination = destination;
    }

    @Override
    public void eventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public int retries() {
        return retries;
    }

    @Override
    public void retries(int retries) {
        this.retries = retries;
    }

}
