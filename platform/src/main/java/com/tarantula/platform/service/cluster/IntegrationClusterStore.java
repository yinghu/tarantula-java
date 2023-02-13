package com.tarantula.platform.service.cluster;

import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.MultiMap;
import com.icodesoftware.service.ClusterProvider;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class IntegrationClusterStore implements ClusterProvider.ClusterStore {

    private final MultiMap<String, byte[]> mIndex;
    private final IMap<byte[],byte[]> vMap;
    private final IQueue<byte[]> vQueue;
    private final long operationTimeout;
    private final IntegrationCluster integrationCluster;
    private final String name;

    public IntegrationClusterStore(IntegrationCluster integrationCluster,String name,MultiMap<String, byte[]> mIndex, IMap<byte[],byte[]> vMap,IQueue<byte[]> vQueue,long operationTimeout){
        this.integrationCluster = integrationCluster;
        this.name = name;
        this.mIndex = mIndex;
        this.vMap = vMap;
        this.vQueue = vQueue;
        this.operationTimeout = operationTimeout;
    }

    public String name(){
        return this.name;
    }

    public byte[] mapGet(byte[] key){
        if(this.vMap==null) throw new RuntimeException("Map Operation not enabled");
        return this.vMap.get(key);
    }

    public byte[] mapSetIfAbsent(byte[] key,byte[] value){
        if(this.vMap==null) throw new RuntimeException("Map Operation not enabled");
        return vMap.putIfAbsent(key,value);
    }
    public void mapSet(byte[] key,byte[] value){
        if(this.vMap==null) throw new RuntimeException("Map Operation not enabled");
        this.vMap.put(key,value);
    }
    public boolean mapExists(byte[] key){
        if(this.vMap==null) throw new RuntimeException("Map Operation not enabled");
        return this.vMap.containsKey(key);
    }
    public byte[] mapRemove(byte[] key){
        if(this.vMap==null) throw new RuntimeException("Map Operation not enabled");
        return vMap.remove(key);
    }

    //key value index list pair
    public void indexSet(String index,byte[] key){
        if(this.mIndex==null) throw new RuntimeException("Index Operation not enabled");
        mIndex.put(index,key);
    }

    public void indexRemove(String index,byte[] key){
        if(this.mIndex==null) throw new RuntimeException("Index Operation not enabled");
        mIndex.remove(index,key);
    }

    public Collection<byte[]> indexGet(String index){
        if(this.mIndex==null) throw new RuntimeException("Index Operation not enabled");
        return mIndex.get(index);
    }

    public void indexRemove(String index){
        if(this.mIndex==null) throw new RuntimeException("Index Operation not enabled");
        mIndex.remove(index);
    }

    public void mapLock(byte[] key){
        if(this.vMap==null) throw new RuntimeException("Map Operation not enabled");
        vMap.lock(key);
    }

    public boolean tryMapLock(byte[] key){
        if(this.vMap==null) throw new RuntimeException("Map Operation not enabled");
        return vMap.tryLock(key);
    }
    public boolean tryMapLock(byte[] key, long time, TimeUnit timeUnit){
        if(this.vMap==null) throw new RuntimeException("Map Operation not enabled");
        try{
            return vMap.tryLock(key,time,timeUnit);
        }catch (Exception ex){
            return false;
        }
    }
    public void mapUnlock(byte[] key){
        if(this.vMap==null) throw new RuntimeException("Map Operation not enabled");
        vMap.unlock(key);
    }

    public boolean queueOffer(byte[] value){
        if(this.vQueue==null) throw new RuntimeException("Queue Operation not enabled");
        try{
            return vQueue.offer(value,operationTimeout, TimeUnit.SECONDS);
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    public byte[] queuePoll(){
        if(this.vQueue==null) throw new RuntimeException("Queue Operation not enabled");
        try{
            return vQueue.poll(operationTimeout, TimeUnit.SECONDS);
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public void clear(){
        clear(true,true,true);
    }

    public void clear(boolean map,boolean index,boolean queue){
        if(map && vMap != null) { vMap.clear(); vMap.destroy();}
        if(index && mIndex != null) { mIndex.clear(); mIndex.destroy();}
        if(queue && vQueue != null) { vQueue.clear(); vQueue.destroy();}
        integrationCluster.closeClusterStore(name);
    }

}
