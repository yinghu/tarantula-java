package com.tarantula.platform.service.persistence;

import com.icodesoftware.service.OnReplication;
import com.icodesoftware.util.UnsafeUtil;
import sun.misc.Unsafe;

public class OffHeapDataScopeReplication implements ScopedOnReplication{

    private Unsafe unsafe;
    private long memoryAddress;

    public OffHeapDataScopeReplication(){
        unsafe = UnsafeUtil.useUnsafe();
    }
    public void write(String sourceNode,String source, byte[] key, byte[] value){
        unsafe = UnsafeUtil.useUnsafe();
        byte[] src = source.getBytes();
        byte[] node = sourceNode.getBytes();
        int nodeLength = node.length;
        int sourceLength = src.length;
        int keyLength = key.length;
        int valueLength = value.length;
        memoryAddress = unsafe.allocateMemory(nodeLength+sourceLength+keyLength+valueLength+16);
        long mp = memoryAddress;
        unsafe.putInt(mp,nodeLength);
        mp += 4;
        unsafe.putInt(mp,sourceLength);
        mp += 4;
        unsafe.putInt(mp,keyLength);
        mp += 4;
        unsafe.putInt(mp,valueLength);
        mp += 4;
        for(byte b : node){
            unsafe.putByte(mp++,b);
        }
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
        long mp = memoryAddress;
        byte[] node = new byte[unsafe.getInt(mp)];
        mp += 4;
        byte[] src = new byte[unsafe.getInt(mp)];
        mp += 4;
        byte[] key = new byte[unsafe.getInt(mp)];
        mp += 4;
        byte[] value = new byte[unsafe.getInt(mp)];
        mp += 4;
        for(int i=0;i<node.length;i++){
            node[i]=unsafe.getByte(mp++);
        }
        for(int i=0;i<src.length;i++){
            src[i]=unsafe.getByte(mp++);
        }
        for(int i=0;i<key.length;i++){
            key[i]=unsafe.getByte(mp++);
        }
        for(int i=0;i<value.length;i++){
            value[i]=unsafe.getByte(mp++);
        }
        unsafe.freeMemory(memoryAddress);
        return new ReplicationData(new String(node),null,new String(src),key,value);
    }

    public void drop(){
        unsafe.freeMemory(memoryAddress);
    }

}
