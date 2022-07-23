package com.tarantula.platform.service.cluster;

import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import com.icodesoftware.service.ClusterProvider;

import java.util.Collection;

public class IntegrationClusterStore implements ClusterProvider.ClusterStore {

    private final MultiMap<String, byte[]> mIndex;
    private final IMap<byte[],byte[]> vMap;

    public IntegrationClusterStore(MultiMap<String, byte[]> mIndex, IMap<byte[],byte[]> vMap){
        this.mIndex = mIndex;
        this.vMap = vMap;
    }

    public byte[] get(byte[] key){
        return this.vMap.get(key);
    }

    public byte[] createIfAbsent(byte[] key,byte[] value){
        byte[] ret = vMap.putIfAbsent(key,value);
        return ret!=null?ret:value;
    }
    public void set(byte[] key,byte[] value){
        this.vMap.put(key,value);
    }

    public byte[] remove(byte[] key){
        return vMap.remove(key);
    }

    //key value index list pair
    public void index(String index,byte[] key){
        mIndex.put(index,key);
    }

    public void removeIndex(String index,byte[] key){
        mIndex.remove(index,key);
    }

    public Collection<byte[]> index(String index){
        return mIndex.get(index);
    }

    public void removeIndex(String index){
        mIndex.remove(index);
    }

}
