package com.tarantula.platform.service.persistence;

import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.OnReplication;
import com.icodesoftware.util.UnsafeUtil;
import sun.misc.Unsafe;

public class OffHeapDataScopeReplication implements ScopedOnReplication{

    private Unsafe unsafe;
    private long memoryAddress;
    private ClusterProvider.Node node;
    public OffHeapDataScopeReplication(ClusterProvider.Node node,String source, byte[] key, byte[] value){
        this.node = node;
        unsafe = UnsafeUtil.useUnsafe();
        byte[] src = source.getBytes();
        int sourceLength = src.length;
        int keyLength = key.length;
        int valueLength = value.length;
        memoryAddress = unsafe.allocateMemory(sourceLength+keyLength+valueLength+12);
        long mp = memoryAddress;
        unsafe.putInt(mp,sourceLength);
        mp += 4;
        unsafe.putInt(mp,keyLength);
        mp += 4;
        unsafe.putInt(mp,valueLength);
        mp += 4;
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
        byte[] src = new byte[unsafe.getInt(mp)];
        mp += 4;
        byte[] key = new byte[unsafe.getInt(mp)];
        mp += 4;
        byte[] value = new byte[unsafe.getInt(mp)];
        mp += 4;
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
        return new ReplicationData(node.nodeName(),new String(src),key,value);
    }

    public void drop(){
        unsafe.freeMemory(memoryAddress);
    }
    public void node(ClusterProvider.Node node){
        this.node = node;
    }
    @Override
    public ClusterProvider.Node node() {
        return node;
    }
}
