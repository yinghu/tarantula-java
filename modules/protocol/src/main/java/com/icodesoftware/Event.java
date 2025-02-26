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

	default RoutingKey routingKey(){ return null;}
	default void registerStreamingListener(StreamingListener listener){}

	interface EventOnTopic extends Event{}
	interface EventOnSession extends Event{}
	interface StreamingListener{
		void onCode(int streamCode);
	}
}
