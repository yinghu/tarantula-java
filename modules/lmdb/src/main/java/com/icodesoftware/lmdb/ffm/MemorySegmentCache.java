package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.BufferProxy;

import java.lang.foreign.Arena;
import java.lang.foreign.ValueLayout;
import java.util.concurrent.ArrayBlockingQueue;

public class MemorySegmentCache implements Recoverable.DataBufferPair{

    private Recoverable.DataBuffer key;
    private Recoverable.DataBuffer value;
    private final ArrayBlockingQueue<MemorySegmentCache> bufferQueue;
    private final Arena arena = Arena.ofShared();

    public MemorySegmentCache(final int keySize,final int valueSize,ArrayBlockingQueue<MemorySegmentCache> bufferQueue){
        this.key = BufferProxy.buffer(arena.allocate(ValueLayout.JAVA_BYTE,keySize));
        this.value = BufferProxy.buffer(arena.allocate(ValueLayout.JAVA_BYTE,valueSize));
        this.bufferQueue = bufferQueue;
    }

    @Override
    public Recoverable.DataBuffer key() {
        return key;
    }

    @Override
    public Recoverable.DataBuffer value() {
        return value;
    }

    @Override
    public void close() {
        reset();
    }

    @Override
    public void reset() {
        key.pointer().fill((byte)0);
        value.pointer().fill((byte)0);
        key.clear();
        value.clear();
        if(!bufferQueue.offer(this)){
            arena.close();
        }
    }
}
