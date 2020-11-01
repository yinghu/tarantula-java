package com.icodesoftware.integration.channel;

import com.icodesoftware.util.FIFOBuffer;

import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 10/31/2020.
 */
public class PendingSession {
    public final FIFOBuffer<Integer> ackBuffer;
    public int messageId;
    public ByteBuffer pending;

    public int retries;

    public PendingSession(){
        ackBuffer = new FIFOBuffer(20,new Integer[20]);
        retries = 2;
    }
}
