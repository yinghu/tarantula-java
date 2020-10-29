package com.icodesoftware.protocol;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 10/24/2020.
 */
public class PendingMessage {
    public static final int INBOUND = 0;
    public static final int OUTBOUND = 1;
    public static final int RETRY = 2;
    public ByteBuffer data;
    public OutboundMessage outboundMessage;
    public int messageId;
    public int sessionId;
    public long connectionId;
    public boolean ack;
    public long timestamp;
    public int retries;
    public MessageHandler callback;
    public int pendingType;
    public SocketAddress source;

    //retry
    public PendingMessage(ByteBuffer data,long timestamp,int retries){
        this.data = data;
        this.timestamp = timestamp;
        this.retries = retries;
        this.pendingType = RETRY;
    }
    public PendingMessage(ByteBuffer data,long timestamp,int retries,MessageHandler callback){
        this(data,timestamp,retries);
        this.callback = callback;
    }
    //inbound
    public PendingMessage(ByteBuffer inboundMessage,SocketAddress source){
        this.data = inboundMessage;
        this.source = source;
        this.pendingType = INBOUND;
    }
    //outbound
    public PendingMessage(OutboundMessage outboundMessage,SocketAddress source){
        this.outboundMessage = outboundMessage;
        this.source = source;
        this.pendingType = OUTBOUND;
    }
    public PendingMessage(OutboundMessage outboundMessage,SocketAddress source,long connectionId,int sessionId,int messageId,boolean ack){
        this(outboundMessage,source);
        this.connectionId = connectionId;
        this.sessionId = sessionId;
        this.messageId = messageId;
        this.ack = ack;
    }
    public PendingMessage(OutboundMessage outboundMessage,SocketAddress source,long connectionId,int sessionId,int messageId,boolean ack,MessageHandler callback){
        this(outboundMessage,source,connectionId,sessionId,messageId,ack);
        this.callback = callback;
    }

}
