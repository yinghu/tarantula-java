package com.icodesoftware.etcd;

public class NodeJoinListener implements ETCDWatchListener{

    @Override
    public String watchKey() {
        return WatchEvent.JOIN;
    }

    @Override
    public void onWatched(EtcdEvent event) {
        EtcdManager.joined(event);
    }
}
