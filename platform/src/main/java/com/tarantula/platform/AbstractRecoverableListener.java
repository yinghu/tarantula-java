package com.tarantula.platform;

import com.tarantula.Metadata;
import com.tarantula.Recoverable;
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
    public void onUpdated(Metadata metadata, byte[] key, byte[] value) {
        OnFilter onFilter = oMap.get(metadata.classId());
        if(onFilter!=null){
            Recoverable recoverable = this.create(metadata.classId());
            if(recoverable.binary()){
                recoverable.fromByteArray(value);
            }
            else{
                recoverable.fromMap(SystemUtil.toMap(value));
            }
            recoverable.distributionKey(new String(key));
            onFilter.onUpdated(recoverable);
        }
    }
    @Override
    public void addRecoverableFilter(int classId,Filter recoverableFilter){
        oMap.computeIfAbsent(classId,(cid)->new OnFilter()).list.add(recoverableFilter);
    }
    public void removeRecoverableFilter(int classId){
        oMap.remove(classId);
    }
    abstract public int registryId();

    abstract public Recoverable create(int i);

    private static class OnFilter{
        List<Filter> list = new CopyOnWriteArrayList<>();
        public void onUpdated(Recoverable t){
            list.forEach((r)->{
                r.on(t);
            });
        }
    }
}
