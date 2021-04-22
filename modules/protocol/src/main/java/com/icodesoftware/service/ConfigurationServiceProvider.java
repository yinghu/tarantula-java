package com.icodesoftware.service;

import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;

import java.util.List;

public interface ConfigurationServiceProvider{

    <T extends Configurable> void register(T configurable);
    <T extends Configurable> void release(T configurable);
    void configure(String key);

    List<Configuration> list(String type);

    void registerConfigurableListener(String type,Configurable.Listener listener);
}
