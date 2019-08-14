package com.tarantula.cci.tcp;

import com.tarantula.Event;
import com.tarantula.cci.OnExchange;
import com.tarantula.platform.event.ResponsiveEvent;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

/**
 * Created by yinghu lu on 10/23/2018.
 */
public class SocketSession implements OnExchange {

    private final String id;
    private final String clientId;
    private final PendingRequest pendingRequest;
    private final String path;
    private final Map<String,Object> headers;
    private final byte[] payload;
    private final boolean streaming;

    public SocketSession(PendingRequest pendingRequest,PendingData pendingData){
        this.id = UUID.randomUUID().toString();
        this.clientId = pendingData.clientId;
        this.pendingRequest = pendingRequest;
        this.path = pendingData.path;
        this.headers = pendingData.headers;
        this.payload = pendingData.payload;
        this.streaming = pendingData.streaming;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public String path() {
        return this.path;
    }

    @Override
    public String method() {
        return "socket";
    }

    @Override
    public String header(String name) {
        return (String) this.headers.get(name);
    }

    @Override
    public byte[] payload() {
        return this.payload;
    }

    @Override
    public String query() {
        return null;
    }

    @Override
    public String remoteAddress() {
        return "0.0.0.0";
    }

    @Override
    public boolean streaming() {
        return this.streaming;
    }

    @Override
    public void onError(Exception ex, String message) {

    }

    @Override
    public boolean onEvent(Event event) {
        ResponsiveEvent responsiveEvent = (ResponsiveEvent)event;
        responsiveEvent.clientId(this.clientId);
        ByteBuffer buffer = pendingRequest.toByteBuffer(responsiveEvent);
        pendingRequest.writeBuffer(buffer);
        if(responsiveEvent.closed()){
            return true;
        }
        return !streaming;
    }
}
