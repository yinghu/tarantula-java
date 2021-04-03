package com.tarantula.platform.service;


public interface Closable extends Runnable {
    void close();
}
