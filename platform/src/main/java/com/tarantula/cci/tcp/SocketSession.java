package com.tarantula.cci.tcp;

import com.tarantula.Event;
import com.tarantula.cci.OnExchange;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

/**
 * Updated by yinghu lu on 12/16/2019.
 */
public class SocketSession implements OnExchange {

    private final String id;
    private final String clientId;
    private final PendingRequest pendingRequest;
    private final String path;
    private final Map<String,Object> headers;
    private final byte[] payload;
    private final boolean streaming;
    private final boolean oneWay;

    public SocketSession(PendingRequest pendingRequest,PendingData pendingData){
        if(!pendingData.oneWay){
            this.id = UUID.randomUUID().toString();
        }
        else{
            id="";
        }
        this.clientId = pendingData.clientId;
        this.pendingRequest = pendingRequest;
        this.path = pendingData.path;
        this.headers = pendingData.headers;
        this.payload = pendingData.payload;
        this.streaming = pendingData.streaming;
        this.oneWay = pendingData.oneWay;
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
    public boolean streaming() {
        return this.streaming;
    }
    @Override
    public boolean oneWay(){
        return this.oneWay;
    }
    @Override
    public boolean onEvent(Event event) {
        event.clientId(this.clientId);
        ByteBuffer buffer = pendingRequest.toByteBuffer(event);
        pendingRequest.writeBuffer(buffer);
        if(event.closed()){
            return true;
        }
        return !streaming;
    }
    public void close(){
        pendingRequest.close();
    }
}
