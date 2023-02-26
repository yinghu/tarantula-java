package com.icodesoftware.service;

import com.icodesoftware.Event;
import com.icodesoftware.EventListener;
import com.icodesoftware.RoutingKey;

public interface EventService extends EventListener, Serviceable {

	void publish(Event out);
    void retry(String retryKey);

    //delegates topic subscribe
    void registerEventListener(String topic, EventListener callback);
    void unregisterEventListener(String topic);
    RoutingKey routingKey(String magicKey,String tag);
    RoutingKey routingKey(String magicKey,String tag,int routingNumber);
}
