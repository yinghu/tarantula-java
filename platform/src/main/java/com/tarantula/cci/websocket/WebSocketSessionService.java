package com.tarantula.cci.websocket;

import com.icodesoftware.Event;
import com.icodesoftware.EventListener;
import com.icodesoftware.RoutingKey;
import com.icodesoftware.service.EventService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

/**
 * Created by yinghu lu on 9/25/2020.
 */
public class WebSocketSessionService implements EventService,WebSocket.Listener {

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
        URI uri = new URI("ws://10.0.0.234:8000");
        WebSocket webSocket = HttpClient.newHttpClient().newWebSocketBuilder().buildAsync(uri,this).join();
    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void onOpen(WebSocket webSocket) {
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        // How do I return the CompletionStage object
        return null;//CompletionStage<String>//
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {

    }
}
