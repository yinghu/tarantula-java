package com.icodesoftware.lmdb;


import com.icodesoftware.Recoverable;

import java.util.concurrent.ArrayBlockingQueue;

public class BufferCache implements Recoverable.DataBufferPair {

    private final Recoverable.DataBuffer key;
    private final Recoverable.DataBuffer value;

    private ArrayBlockingQueue<BufferCache> bufferQueue;

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

    @Override
    public Recoverable.DataBuffer key() {
        return key;
    }

    @Override
    public Recoverable.DataBuffer value() {
        return value;
    }

    public void close(){
        reset();
    }
}
