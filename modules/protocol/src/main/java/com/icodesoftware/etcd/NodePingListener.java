package com.icodesoftware.etcd;

public class NodePingListener implements ETCDWatchListener{


    @Override
    public String watchKey() {
        return WatchEvent.PING;
    }

    @Override
    public void onWatched(EtcdEvent event) {
        EtcdManager.ping(EtcdNode.create(event.nodeName));
    }
}
