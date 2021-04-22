package com.icodesoftware.service;

import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;

import java.util.List;

public interface ConfigurationServiceProvider{
    void register(Configurable configurable);
    void release(Configurable configurable);
    void configure(String key);
    List<Configuration> list(String type);

    void registerConfigurableListener(String type,Configurable.Listener listener);
}
