package com.tarantula.platform.service;

import com.icodesoftware.EventListener;


public interface BucketReceiver extends EventListener {

    int OPEN = 0;
    int CLOSE = 1;
    int SHUT_DOWN = 2;

    int partition();
    String bucket();
    boolean opening();
    void open();
    void close();
    void shutdown();
}
