package com.icodesoftware.etcd;

public class NodeClaimListener implements ETCDWatchListener{

    @Override
    public String watchKey() {
        return ClaimEvent.CLAIM;
    }

    @Override
    public void onWatched(EtcdEvent event) {
        ClaimEvent claimEvent = (ClaimEvent)event;
        EtcdManager.claim(EtcdNode.create(claimEvent.nodeName,claimEvent.httpEndpoint));
    }
}
