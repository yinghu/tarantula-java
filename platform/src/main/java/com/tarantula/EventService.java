package com.tarantula;

import com.tarantula.platform.service.Serviceable;

public interface EventService extends EventListener, Serviceable {

	void publish(Event out);
	String subscription();
    void retry(String retryKey);

    void registerEventListener(String topic, EventListener callback);

    RoutingKey instanceRoutingKey(String applicationId,String instanceId);
    RoutingKey routingKey(String magicKey,String tag);
    RoutingKey routingKey(String magicKey,String tag,int routingNumber);
}
