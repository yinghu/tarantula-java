package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

public interface ServiceEvent extends Recoverable {

    enum Level{CRITICAL,SEVERE,WARN}

    int code();
    Level level();
    String source();
    String message();
    String stackTrace();
}
