package com.icodesoftware;


import com.icodesoftware.service.ServiceContext;

public interface Configurable extends Recoverable, DataStore.Updatable {

    default void registerListener(Listener listener){}
    default void update(ServiceContext serviceContext){}

    default String configurationType(){return null;}
    default void configurationType(String configurationType){}
    default String configurationName(){return null;}
    default void configurationName(String configurationName){}

    interface Listener<T extends Configurable>{
        void onUpdated(T updated);
    }
}