package com.icodesoftware.service;

import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;

import java.util.List;

public interface ConfigurationServiceProvider{

    default <T extends Configurable> void create(T configurable){}

    <T extends Configurable> void register(T configurable);
    <T extends Configurable> void release(T configurable);
    void configure(String key);

    <T extends Configuration> List<T> configurations(String type);

    String registerConfigurableListener(String type,Configurable.Listener listener);
    void unregisterConfigurableListener(String registryKey);
}
