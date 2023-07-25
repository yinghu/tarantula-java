package com.tarantula.platform.service;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.ServiceEvent;
import com.icodesoftware.service.ServiceEventListener;
import com.icodesoftware.service.ServiceEventLogger;
import com.tarantula.platform.util.SystemUtil;

import java.util.concurrent.ConcurrentHashMap;

public class PlatformServiceEventLogger implements ServiceEventLogger {


    private ConcurrentHashMap<String,ServiceEventListener> listeners = new ConcurrentHashMap<>();


    private DataStore dataStore;
    private boolean persistenceEnabled;
    public PlatformServiceEventLogger(DataStore dataStore,boolean persistenceEnable){
        this.dataStore = dataStore;
        this.persistenceEnabled = persistenceEnable;
    }
    @Override
    public void log(ServiceEvent event) {
        if(persistenceEnabled) dataStore.create(event);
        listeners.forEach((k,l)->l.onEvent(event));
    }


    public String registerServiceEventListener(ServiceEventListener listener){
        String registerKey = SystemUtil.oid();
        listeners.put(registerKey,listener);
        return registerKey;
    }

    public void unregisterServiceEventListener(String registerKey){
        listeners.remove(registerKey);
    }
}