package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.BufferProxy;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;


public class NativeData {

    private MemorySegment pointer;
    private MemorySegment data;
    private StructLayout struct;
    private final boolean reading;

    private NativeData(Arena arena,long size){
        this.struct = MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withName("mv_size"),ValueLayout.ADDRESS.withName("mv_data"));
        this.pointer = arena.allocate(struct);
        this.data = arena.allocate(ValueLayout.JAVA_BYTE,size);
        this.reading = false;
    }

    private NativeData(Arena arena){
        this.struct = MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withName("mv_size"),ValueLayout.ADDRESS.withName("mv_data"));
        this.pointer = arena.allocate(struct);
        this.reading = true;
    }

    public NativeData write(OnData onData){
        if(reading) throw new RuntimeException("read buffer");
        Recoverable.DataBuffer buffer = BufferProxy.buffer(data);
        onData.fill(buffer);
        buffer.writeByte((byte)'\0');
        buffer.flip();
        VarHandle vSize = struct.varHandle(MemoryLayout.PathElement.groupElement("mv_size"));
        vSize.set(pointer,0,buffer.remaining());
        VarHandle vData = struct.varHandle(MemoryLayout.PathElement.groupElement("mv_data"));
        vData.set(pointer,0,data);
        return this;
    }
    public MemorySegment read(OnData onData){
        if(reading) throw new RuntimeException("read buffer");
        onData.fill(BufferProxy.buffer(data));
        return this.pointer;
    }

    public void read(Arena arena,OnData onData){
        if(!reading) throw new RuntimeException("write buffer");
        MemorySegment data = pointer.get(ValueLayout.ADDRESS,8);
        long len = pointer.get(ValueLayout.JAVA_LONG,0);
        onData.fill(BufferProxy.buffer(data.reinterpret(len,arena,null)));
    }

    public MemorySegment pointer(){
        return pointer;
    }

    public static NativeData in(Arena arena,long size){
        return new NativeData(arena,size);
    }

    public static NativeData out(Arena arena){
        return new NativeData(arena);
    }


}
