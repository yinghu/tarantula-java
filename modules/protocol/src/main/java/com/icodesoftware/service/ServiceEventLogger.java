package com.icodesoftware.service;

public interface ServiceEventLogger {
    void log(ServiceEvent event);
    void flush();
}
