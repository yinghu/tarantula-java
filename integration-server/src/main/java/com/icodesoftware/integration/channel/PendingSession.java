package com.icodesoftware.integration.channel;

import com.icodesoftware.util.FIFOBuffer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by yinghu lu on 10/31/2020.
 */
public class PendingSession {
    public final FIFOBuffer<Integer> ackBuffer;
    public int messageId;
    public byte[] data;
    public long timestamp;
    public int retries;
    public AtomicBoolean pending;
    public PendingSession(){
        ackBuffer = new FIFOBuffer(20,new Integer[20]);
        retries = 2;
        pending = new AtomicBoolean(false);
    }
}
