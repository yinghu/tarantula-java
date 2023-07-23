package com.icodesoftware.service;

public interface ServiceEventLogger {
    void log(ServiceEvent event);
    void save(ServiceEvent event);
    boolean load(ServiceEvent event);
    void flush();

    void registerServiceEventListener(ServiceEventListener listener);
}
