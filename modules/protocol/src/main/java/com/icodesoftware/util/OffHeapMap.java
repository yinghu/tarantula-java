package com.icodesoftware.util;

import sun.misc.Unsafe;

import java.util.concurrent.ConcurrentHashMap;

public class OffHeapMap {

    private ConcurrentHashMap<String,OffHeapIndex> offHeapIndex = new ConcurrentHashMap<>();
    private Unsafe unsafe;

    public OffHeapMap(){
        unsafe = UnsafeUtil.useUnsafe();
    }

    public byte[] get(String key){
        OffHeapIndex index = offHeapIndex.compute(key,(k,idx)->{
            if(idx==null) return null;
            idx.payload = new byte[idx.length];
            long offset = 0;
            for(int i=0;i<idx.length;i++){
                idx.payload[i]=unsafe.getByte(idx.address+offset);
                offset += 1;
            }
            return idx;
        });
        return index!=null?index.payload:null;
    }
    public void set(String key,byte[] value){
        offHeapIndex.compute(key,(k,index)->{
            if(index==null){
                index = new OffHeapIndex();
                index.address = unsafe.allocateMemory(value.length);
                index.length = value.length;
                _set(index,value);
                return index;
            }
            unsafe.freeMemory(index.address);
            index.address = unsafe.allocateMemory(value.length);
            index.length = value.length;
            index.payload = null;
            _set(index,value);
            return index;
        });
    }
    private void _set(OffHeapIndex index,byte[] value){
        long offset = 0;
        for(byte b : value){
            unsafe.putByte(index.address+offset,b);
            offset +=1;
        }
    }
    public void clear(){
        offHeapIndex.forEach((k,i)->{
            unsafe.freeMemory(i.address);
        });
        offHeapIndex.clear();
    }

    private static class OffHeapIndex{
        public long address;
        public int length;
        public byte[] payload;
    }
}
