package com.icodesoftware.integration.channel;

import com.icodesoftware.util.FIFOBuffer;

/**
 * Created by yinghu lu on 10/31/2020.
 */
public class PendingSession {
    public final FIFOBuffer<Integer> ackBuffer;

    public PendingSession(){
        ackBuffer = new FIFOBuffer(20,new Integer[20]);
    }
}
