package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

public interface ServiceEvent extends Recoverable {

    enum Level{CRITICAL,WARN,INFO}
    String code();
    Level level();
    String source();
    String message();
    String stackTrace();
}
