package com.tarantula.platform;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.RecoverableListener;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Updated by yinghu lu on 12/18/2020
 */
public abstract class AbstractRecoverableListener implements RecoverableListener {


    private ConcurrentHashMap<Integer,OnFilter> oMap = new ConcurrentHashMap<>();

    @Override
    public void onUpdated(int classId,String owner,String key, byte[] value) {
        OnFilter onFilter = oMap.get(classId);
        if(onFilter!=null){
            Recoverable recoverable = this.create(classId);
            recoverable.fromBinary((value));
            recoverable.owner(owner);
            recoverable.distributionKey(key);
            onFilter.onUpdated(recoverable);
        }
    }
    @Override
    public void addRecoverableFilter(int classId,Filter recoverableFilter){
        oMap.computeIfAbsent(classId,(cid)->new OnFilter()).list.add(recoverableFilter);
    }
    abstract public int registryId();

    abstract public Recoverable create(int i);
    public <T extends Recoverable> RecoverableFactory<T> query(int registerId, String[] params){
        return null;
    }
    private static class OnFilter{
        List<Filter> list = new CopyOnWriteArrayList<>();
        public void onUpdated(Recoverable t){
            list.forEach((r)->{
                r.on(t);
            });
        }
    }
}
