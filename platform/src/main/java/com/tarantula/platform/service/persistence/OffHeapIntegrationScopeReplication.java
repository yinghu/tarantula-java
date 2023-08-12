package com.tarantula.platform.service.persistence;

import com.icodesoftware.service.OnReplication;
import com.icodesoftware.util.UnsafeUtil;
import sun.misc.Unsafe;

public class OffHeapIntegrationScopeReplication implements ScopedOnReplication {

    private Unsafe unsafe;
    private long memoryAddress;
    private int partition;
    private int keyLength;
    private int valueLength;


    public OffHeapIntegrationScopeReplication(int partition,byte[] key, byte[] value){
        unsafe = UnsafeUtil.useUnsafe();
        this.partition = partition;
        keyLength = key.length;
        valueLength = value.length;
        memoryAddress = unsafe.allocateMemory(keyLength+valueLength);
        long mp = memoryAddress;
        for(byte b : key){
            unsafe.putByte(mp++,b);
        }
        for(byte b : value){
            unsafe.putByte(mp++,b);
        }
    }

    public OnReplication read(){
        byte[] key = new byte[keyLength];
        byte[] value = new byte[valueLength];
        long mp = memoryAddress;
        for(int i=0;i<keyLength;i++){
            key[i]=unsafe.getByte(mp++);
        }
        for(int i=0;i<valueLength;i++){
            value[i]=unsafe.getByte(mp++);
        }
        unsafe.freeMemory(memoryAddress);
        return new ReplicationData(partition,key,value);
    }

    public void drop(){
        unsafe.freeMemory(memoryAddress);
    }

}
