package com.icodesoftware.service;

import com.icodesoftware.Configurable;
import com.icodesoftware.Configuration;
import com.icodesoftware.Descriptor;


public interface ConfigurationServiceProvider extends ServiceProvider{

    default <T extends Configurable> void register(T configurable){}
    default <T extends Configurable> void release(T configurable){}

    default <T extends Configuration> T configuration(String config){throw new UnsupportedOperationException();}

    default String registerConfigurableListener(Descriptor application, Configurable.Listener listener){throw new UnsupportedOperationException();}
    default String registerConfigurableListener(String category, Configurable.Listener listener){ throw new UnsupportedOperationException();}

    default void unregisterConfigurableListener(String registryKey){}

}
