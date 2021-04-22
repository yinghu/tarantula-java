package com.icodesoftware;


import com.icodesoftware.service.ServiceContext;

public interface Configurable extends Recoverable, DataStore.Updatable {

    default void registerListener(Listener listener){}
    default void update(ServiceContext serviceContext){}

    String type();
    void type(String type);

    interface Listener{
        void onUpdated(Configurable updated);
    }
}