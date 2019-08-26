package com.tarantula;

/**
 * Event represents an asynchronous message
 * Updated by yinghu lu on 8/23/2019
 * */
public interface Event extends Session{

    String tag();
    void tag(String tag);

	String destination();
	void destination(String destination);

	void eventService(EventService eventService);

	int retries();
	void retries(int retries);

}
