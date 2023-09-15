package com.icodesoftware.lmdb;


import com.icodesoftware.Recoverable;

import java.util.concurrent.ArrayBlockingQueue;

public class BufferCache {

    public final Recoverable.DataBuffer key;
    public final Recoverable.DataBuffer value;

    private final ArrayBlockingQueue<BufferCache> bufferQueue;

    public BufferCache(final int keySize,final int valueSize,ArrayBlockingQueue<BufferCache> bufferQueue){
        this.key = BufferProxy.buffer(keySize,true);
        this.value = BufferProxy.buffer(valueSize,true);
        this.bufferQueue = bufferQueue;
    }

    public void reset(){
        key.clear();
        value.clear();
        bufferQueue.offer(this);
    }

}
