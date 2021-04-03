package com.icodesoftware;

public interface TarantulaLogger {

    void debug(String message);
    void info(String message);
    void warn(String message);
    void warn(String message,Throwable warn);
    void error(String message,Throwable error);
}
