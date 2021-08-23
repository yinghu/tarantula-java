package com.icodesoftware.service;

import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;


public interface ConfigurationServiceProvider extends ServiceProvider{



    <T extends Configurable> void register(T configurable);
    <T extends Configurable> void release(T configurable);
    void configure(String key);

    <T extends Configuration> T configuration(String config);

    String registerConfigurableListener(String type,Configurable.Listener listener);
    void unregisterConfigurableListener(String registryKey);

}
