package com.icodesoftware.etcd;

public class TopicCreateListener implements ETCDWatchListener{
    @Override
    public String watchKey() {
        return TopicEvent.TOPIC;
    }

    @Override
    public void onWatched(EtcdEvent event) {
        TopicEvent topic  =(TopicEvent) event;
        EtcdManager.topic(topic);
    }
}
