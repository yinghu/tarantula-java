package com.icodesoftware.protocol;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 10/24/2020.
 */
public class PendingMessage {

    public static final int INBOUND = 0;
    public static final int OUTBOUND = 1;

    public ByteBuffer data;
    public long timestamp;
    public int retries;
    public MessageHandler callback;
    public int pendingType;
    public SocketAddress source;

    public PendingMessage(ByteBuffer data,long timestamp,int retries){
        this.data = data;
        this.timestamp = timestamp;
        this.retries = retries;
    }
    public PendingMessage(ByteBuffer data,long timestamp,int retries,MessageHandler callback){
        this(data,timestamp,retries);
        this.callback = callback;
    }
    //inbound/outbound
    public PendingMessage(ByteBuffer inboundMessage,SocketAddress source,int pendingType){
        this.data = inboundMessage;
        this.source = source;
        this.pendingType = pendingType;
    }


}
