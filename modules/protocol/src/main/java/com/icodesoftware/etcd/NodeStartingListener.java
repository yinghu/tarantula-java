package com.icodesoftware.etcd;

import java.util.concurrent.CountDownLatch;

public interface NodeStartingListener {
    void onStarting(CountDownLatch ready);
}
