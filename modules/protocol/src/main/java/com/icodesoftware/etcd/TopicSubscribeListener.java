package com.icodesoftware.etcd;

public class TopicSubscribeListener implements ETCDWatchListener{
    @Override
    public String watchKey() {
        return SubscribeEvent.SUBSCRIBE;
    }

    @Override
    public void onWatched(EtcdEvent event) {
        SubscribeEvent subscribe = (SubscribeEvent) event;
        EtcdManager.subscribe(subscribe);
    }
}
