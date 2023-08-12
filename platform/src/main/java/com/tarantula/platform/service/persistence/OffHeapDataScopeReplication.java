package com.tarantula.platform.service.persistence;

import com.icodesoftware.service.OnReplication;
import com.icodesoftware.util.UnsafeUtil;
import sun.misc.Unsafe;

public class OffHeapDataScopeReplication implements ScopedOnReplication{

    private Unsafe unsafe;
    private long memoryAddress;
    private int sourceLength;
    private int keyLength;
    private int valueLength;


    public OffHeapDataScopeReplication(String source, byte[] key, byte[] value){
        unsafe = UnsafeUtil.useUnsafe();
        byte[] src = source.getBytes();
        sourceLength = src.length;
        keyLength = key.length;
        valueLength = value.length;
        memoryAddress = unsafe.allocateMemory(sourceLength+keyLength+valueLength);
        long mp = memoryAddress;
        for(byte b : src){
            unsafe.putByte(mp++,b);
        }
        for(byte b : key){
            unsafe.putByte(mp++,b);
        }
        for(byte b : value){
            unsafe.putByte(mp++,b);
        }
    }

    public OnReplication read(){
        byte[] src = new byte[sourceLength];
        byte[] key = new byte[keyLength];
        byte[] value = new byte[valueLength];
        long mp = memoryAddress;
        for(int i=0;i<sourceLength;i++){
            src[i]=unsafe.getByte(mp++);
        }
        for(int i=0;i<keyLength;i++){
            key[i]=unsafe.getByte(mp++);
        }
        for(int i=0;i<valueLength;i++){
            value[i]=unsafe.getByte(mp++);
        }
        unsafe.freeMemory(memoryAddress);
        return new ReplicationData(new String(src),key,value);
    }

    public void drop(){
        unsafe.freeMemory(memoryAddress);
    }

}
