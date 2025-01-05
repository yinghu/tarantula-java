package com.icodesoftware.etcd;


public class NodeJoinedListener implements ETCDWatchListener{


    @Override
    public String watchKey() {
        return JoinedEvent.JOINED;
    }

    @Override
    public void onWatched(EtcdEvent event) {
        if(event.nodeName.equals(EtcdManager.localNode.name())) return;
        JoinedEvent joinedEvent = (JoinedEvent)event;
        EtcdManager.claim(EtcdNode.create(joinedEvent.nodeName,joinedEvent.httpEndpoint));
    }
}
