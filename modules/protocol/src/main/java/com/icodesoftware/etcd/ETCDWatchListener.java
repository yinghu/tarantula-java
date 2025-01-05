package com.icodesoftware.etcd;

public interface ETCDWatchListener {
    String watchKey();
    void onWatched(EtcdEvent event);
}
