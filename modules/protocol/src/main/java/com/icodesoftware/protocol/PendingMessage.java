package com.icodesoftware.protocol;

import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 10/24/2020.
 */
public class PendingMessage {
    public ByteBuffer data;
    public long timestamp;
    public int retries;
    public PendingMessage(ByteBuffer data,long timestamp,int retries){
        this.data = data;
        this.timestamp = timestamp;
        this.retries = retries;
    }
}
