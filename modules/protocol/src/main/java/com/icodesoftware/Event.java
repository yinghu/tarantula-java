package com.icodesoftware;

import com.icodesoftware.service.EventService;

public interface Event extends Session {

    String tag();
    void tag(String tag);

	String destination();
	void destination(String destination);

	void eventService(EventService eventService);

	int retries();
	void retries(int retries);

    interface EventOnTopic extends Event{}
	interface EventOnSession extends Event{}
}
