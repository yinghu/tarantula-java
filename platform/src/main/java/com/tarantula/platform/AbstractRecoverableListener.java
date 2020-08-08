package com.tarantula.platform;

import com.tarantula.Recoverable;
import com.tarantula.RecoverableFactory;
import com.tarantula.RecoverableListener;
import com.tarantula.platform.util.SystemUtil;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Updated by yinghu lu on 8/23/2019.
 */
public abstract class AbstractRecoverableListener implements RecoverableListener {


    private ConcurrentHashMap<Integer,OnFilter> oMap = new ConcurrentHashMap<>();

    @Override
    public void onUpdated(int classId,String key, byte[] value) {
        OnFilter onFilter = oMap.get(classId);
        if(onFilter!=null){
            Recoverable recoverable = this.create(classId);
            recoverable.fromMap(SystemUtil.toMap(value));
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
