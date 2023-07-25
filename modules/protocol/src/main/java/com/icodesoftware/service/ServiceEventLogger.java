package com.icodesoftware.service;

public interface ServiceEventLogger {

    void log(ServiceEvent event);


    String registerServiceEventListener(ServiceEventListener listener);
    void unregisterServiceEventListener(String registerKey);
}
