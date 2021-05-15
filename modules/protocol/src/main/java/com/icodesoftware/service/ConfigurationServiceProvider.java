package com.icodesoftware.service;

import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;
import com.icodesoftware.DataStore;

import java.util.List;

public interface ConfigurationServiceProvider{


    default void dataStore(OnDataStore onDataStore){
    }


    <T extends Configurable> void register(T configurable);
    <T extends Configurable> void release(T configurable);
    void configure(String key);

    <T extends Configuration> List<T> configurations(String type);

    String registerConfigurableListener(String type,Configurable.Listener listener);
    void unregisterConfigurableListener(String registryKey);

    interface OnDataStore{
        void on(DataStore dataStore);
    }
}
