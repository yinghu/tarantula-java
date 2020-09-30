package com.tarantula;

import com.icodesoftware.Recoverable;
import com.tarantula.platform.service.ServiceContext;

public interface Configurable extends Recoverable, DataStore.Updatable {

    default void registerListener(Listener listener){}
    default void update(ServiceContext serviceContext){}

    interface Listener{
        void onUpdated(Configurable c);
    }
}
