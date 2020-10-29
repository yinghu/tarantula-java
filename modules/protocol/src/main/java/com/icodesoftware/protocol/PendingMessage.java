package com.icodesoftware.protocol;

import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 10/24/2020.
 */
public class PendingMessage {
    public ByteBuffer data;
    public long timestamp;
    public int retries;
    public MessageHandler callback;
    public InboundMessage inboundMessage;
    public boolean inbound;

    public PendingMessage(ByteBuffer data,long timestamp,int retries){
        this.data = data;
        this.timestamp = timestamp;
        this.retries = retries;
        this.inbound = false;
    }
    public PendingMessage(ByteBuffer data,long timestamp,int retries,MessageHandler callback){
        this(data,timestamp,retries);
        this.callback = callback;
    }
    public PendingMessage(InboundMessage inboundMessage){
        this.inboundMessage = inboundMessage;
        this.inbound = true;
    }
}
