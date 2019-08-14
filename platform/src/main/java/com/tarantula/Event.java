package com.tarantula;

import com.hazelcast.nio.serialization.Portable;

/**
 * Event represents an asynchronous message
 * Updated by yinghu lu on 10/8/2018
 * */
public interface Event extends Session{

    String tag();
    void tag(String tag);

	String destination();
	void destination(String destination);

	void eventService(EventService eventService);

	int retries();
	void retries(int retries);

	void portable(Portable portable);
	Portable portable();

}
