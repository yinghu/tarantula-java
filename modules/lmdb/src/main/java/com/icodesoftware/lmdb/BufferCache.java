package com.icodesoftware.lmdb;


import com.icodesoftware.Recoverable;

import java.util.concurrent.ArrayBlockingQueue;

public class BufferCache {

    public final Recoverable.DataBuffer key;
    public final Recoverable.DataBuffer value;

    private int using = 2;
    private final ArrayBlockingQueue<BufferCache> bufferQueue;

    public BufferCache(final int keySize,final int valueSize,ArrayBlockingQueue<BufferCache> bufferQueue){
        this.key = BufferProxy.buffer(keySize,true,this);
        this.value = BufferProxy.buffer(valueSize,true,this);
        this.bufferQueue = bufferQueue;
    }

    public void reset(){
        using = 2;
        key.clear();
        value.clear();
        bufferQueue.offer(this);
    }
    public void close(){
        using--;
        if(using==0) reset();
    }
}
