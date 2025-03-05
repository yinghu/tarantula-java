package com.icodesoftware.etcd;

import com.icodesoftware.service.ClusterProvider;

import java.util.concurrent.CountDownLatch;

public interface NodeStateListener {
    void onStarting(CountDownLatch ready);
    void onJoined(ClusterProvider.Node nodeJoined);
    void onLeft(ClusterProvider.Node nodeLeft);

}
