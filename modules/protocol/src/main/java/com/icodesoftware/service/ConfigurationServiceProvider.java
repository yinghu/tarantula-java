package com.icodesoftware.service;

import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;
import com.icodesoftware.Descriptor;


public interface ConfigurationServiceProvider extends ServiceProvider{



    <T extends Configurable> void register(T configurable);
    <T extends Configurable> void release(T configurable);
    void configure(String key);

    <T extends Configuration> T configuration(String config);

    String registerConfigurableListener(Descriptor application, Configurable.Listener listener);
    String registerConfigurableListener(String category, Configurable.Listener listener);

    void unregisterConfigurableListener(String registryKey);

}
