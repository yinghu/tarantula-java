package com.tarantula.platform.store;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.ServiceEvent;
import com.icodesoftware.service.ServiceEventListener;
import com.icodesoftware.service.ServiceEventLogger;

public class TransactionEventLogger implements ServiceEventLogger {

    private DataStore dataStore;

    public TransactionEventLogger(DataStore dataStore){
        this.dataStore = dataStore;
    }
    @Override
    public void log(ServiceEvent event) {
        if(!(event instanceof Transaction)) throw new RuntimeException(event.getClass().getName()+" not supported");
        if(!dataStore.createIfAbsent(event,false)) throw new RuntimeException("transaction already existed");
    }

    @Override
    public String registerServiceEventListener(ServiceEventListener listener) {
        return null;
    }

    @Override
    public void unregisterServiceEventListener(String registerKey) {


    }
}
