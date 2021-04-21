package com.icodesoftware.service;

import com.icodesoftware.Configurable;

public interface ConfigurationServiceProvider{
    void register(Configurable configurable);
    void release(Configurable configurable);
}
