package com.icodesoftware.integration.udp;

import com.icodesoftware.protocol.MessageHandler;
import java.net.SocketAddress;

/**
 * Created by yinghu lu on 10/24/2020.
 */
public class PendingMessage {

    public static final int INBOUND = 0;
    public static final int OUTBOUND = 1;

    public byte[] data;
    public long timestamp;
    public int retries;
    public MessageHandler callback;
    public int pendingType;
    public SocketAddress source;
    public Runnable runnable;
    //retry cache
    public PendingMessage(byte[] data, long timestamp, int retries, MessageHandler callback){
        this.data = data;
        this.timestamp = timestamp;
        this.retries = retries;
        this.callback = callback;
        this.pendingType = OUTBOUND;
    }
    //inbound/outbound
    public PendingMessage(byte[] inboundMessage, SocketAddress source){
        this.data = inboundMessage;
        this.source = source;
        this.pendingType = INBOUND;
    }
    public PendingMessage(Runnable runnable){
        this.runnable = runnable;
        this.pendingType = OUTBOUND;
    }


}
