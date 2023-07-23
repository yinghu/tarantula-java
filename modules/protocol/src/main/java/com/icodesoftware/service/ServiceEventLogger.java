package com.icodesoftware.service;

public interface ServiceEventLogger {
    void log(ServiceEvent event);

    boolean load(ServiceEvent event);
    void flush();
}
