package com.tarantula.cci.webhook;

import com.tarantula.Event;
import com.tarantula.EventListener;
import com.tarantula.EventService;
import com.tarantula.RoutingKey;

import java.net.http.WebSocket;

/**
 * Created by yinghu lu on 9/21/2020.
 */
public class WebhookSessionService implements EventService {
    @Override
    public void publish(Event out) {

    }

    @Override
    public String subscription() {
        return null;
    }

    @Override
    public void retry(String retryKey) {

    }

    @Override
    public void registerEventListener(String topic, EventListener callback) {

    }

    @Override
    public RoutingKey instanceRoutingKey(String applicationId, String instanceId) {
        return null;
    }

    @Override
    public RoutingKey routingKey(String magicKey, String tag) {
        return null;
    }

    @Override
    public RoutingKey routingKey(String magicKey, String tag, int routingNumber) {
        return null;
    }

    @Override
    public boolean onEvent(Event event) {
        return false;
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void shutdown() throws Exception {

    }
}
