package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.BufferProxy;

import java.lang.foreign.*;
import java.lang.invoke.VarHandle;


public class NativeData {

    private static final MemoryLayout struct = MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withName("mv_size"),ValueLayout.ADDRESS.withName("mv_data"));

    private NativeData(){}

    public static InVal in(Arena arena,long size){
        return new NativeData.InVal(arena,size);
    }

    public static InPair inPair(Arena arena,long size){
        return new NativeData.InPair(arena,size);
    }

    public static OutVal out(Arena arena){
        return new NativeData.OutVal(arena);
    }

    public static OutPair outPair(Arena arena){
        return new NativeData.OutPair(arena);
    }


    public static class InVal{
        private MemorySegment pointer;
        private MemorySegment data;

        private InVal(Arena arena,long size){
            this.pointer = arena.allocate(struct);
            this.data = arena.allocate(ValueLayout.JAVA_BYTE,size);
        }

        public InVal write(NativeDataWriter onData){
            Recoverable.DataBuffer buffer = BufferProxy.buffer(data);
            onData.onBuffer(buffer);
            buffer.writeByte((byte)'\0');
            buffer.flip();
            VarHandle vSize = struct.varHandle(MemoryLayout.PathElement.groupElement("mv_size"));
            vSize.set(pointer,0,buffer.remaining());
            VarHandle vData = struct.varHandle(MemoryLayout.PathElement.groupElement("mv_data"));
            vData.set(pointer,0,data);
            return this;
        }

        public MemorySegment read(NativeDataWriter onData){
            onData.onBuffer(BufferProxy.buffer(data));
            return this.pointer;
        }

        public MemorySegment pointer(){
            return pointer;
        }
    }

    public static class OutVal{

        private MemorySegment pointer;

        private OutVal(Arena arena){
            this.pointer = arena.allocate(struct);
        }

        public void read(Arena arena, NativeDataWriter onData){
            MemorySegment data = pointer.get(ValueLayout.ADDRESS,8);
            long len = pointer.get(ValueLayout.JAVA_LONG,0);
            onData.onBuffer(BufferProxy.buffer(data.reinterpret(len,arena,null)));
        }

        public MemorySegment pointer(){
            return pointer;
        }
    }

    public static class OutPair{

        private MemorySegment pointer1;
        private MemorySegment pointer2;

        private OutPair(Arena arena){
            this.pointer1 = arena.allocate(struct);
            this.pointer2 = arena.allocate(struct);
        }

        public boolean stream(Arena arena, DataStore.BufferStream stream){
            MemorySegment data1 = pointer1.get(ValueLayout.ADDRESS,8);
            long len1 = pointer1.get(ValueLayout.JAVA_LONG,0);

            MemorySegment data2 = pointer2.get(ValueLayout.ADDRESS,8);
            long len2 = pointer2.get(ValueLayout.JAVA_LONG,0);

            return stream.on(BufferProxy.buffer(data1.reinterpret(len1,arena,null)),BufferProxy.buffer(data2.reinterpret(len2,arena,null)));
        }

        public MemorySegment pointer1(){
            return pointer1;
        }

        public MemorySegment pointer2(){
            return pointer2;
        }
    }

    public static class InPair{
        private MemorySegment pointer1;
        private MemorySegment data1;

        private MemorySegment pointer2;
        private MemorySegment data2;


        private InPair(Arena arena,long size){
            this.pointer1 = arena.allocate(struct);
            this.data1 = arena.allocate(ValueLayout.JAVA_BYTE,size);
            this.pointer2 = arena.allocate(struct);
            this.data2 = arena.allocate(ValueLayout.JAVA_BYTE,size);
        }

        public InPair write(NativeDataPairWriter onData){
            Recoverable.DataBuffer buffer1 = BufferProxy.buffer(data1);
            Recoverable.DataBuffer buffer2 = BufferProxy.buffer(data2);
            onData.onBuffer(buffer1,buffer2);
            buffer1.writeByte((byte)'\0');
            buffer1.flip();
            VarHandle vSize1 = struct.varHandle(MemoryLayout.PathElement.groupElement("mv_size"));
            vSize1.set(pointer1,0,buffer1.remaining());
            VarHandle vData1 = struct.varHandle(MemoryLayout.PathElement.groupElement("mv_data"));
            vData1.set(pointer1,0,data1);

            buffer2.writeByte((byte)'\0');
            buffer2.flip();
            VarHandle vSize2 = struct.varHandle(MemoryLayout.PathElement.groupElement("mv_size"));
            vSize2.set(pointer2,0,buffer2.remaining());
            VarHandle vData2 = struct.varHandle(MemoryLayout.PathElement.groupElement("mv_data"));
            vData2.set(pointer2,0,data2);
            return this;
        }

        public MemorySegment pointer1(){
            return pointer1;
        }

        public MemorySegment pointer2(){
            return pointer2;
        }


    }


}
