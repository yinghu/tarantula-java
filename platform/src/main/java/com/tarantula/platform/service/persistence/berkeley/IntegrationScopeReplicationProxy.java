package com.tarantula.platform.service.persistence.berkeley;

import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.service.Metadata;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.KeyIndexTrack;

public class IntegrationScopeReplicationProxy extends ScopedReplicationProxy {

    private TarantulaLogger logger = JDKLogger.getLogger(IntegrationScopeReplicationProxy.class);

    public IntegrationScopeReplicationProxy(DataStoreProvider dataStoreProvider){
        super(dataStoreProvider);
    }
    @Override
    public <T extends Recoverable> void onBackingUp(Metadata metadata, String key, T t) {

    }
    @Override
    public void onDistributing(Metadata metadata, String stringKey, byte[] key, byte[] value) {
        KeyIndex keyIndex = this.keyIndexService.lookup(metadata.source(),stringKey);
        if(keyIndex==null){
            ClusterProvider.Node[] nodes = nextNodeList(serviceContext.clusterProvider().maxReplicationNumber());
            int replicated = this.serviceContext.clusterProvider().accessIndexService().onReplicate(metadata.partition(),key,value,nodes);
            if(replicated==0) {
                logger.warn("Replication number [" + replicated + "] of " + serviceContext.clusterProvider().maxReplicationNumber() + "]");
                keyIndex = new KeyIndexTrack();
                keyIndex.owner(metadata.source());
                keyIndex.index(stringKey);
                keyIndex.placeMasterNode(localNode.nodeName());
                this.keyIndexService.createIfAbsent(keyIndex);
            }
        }
    }


    @Override
    public byte[] onRecovering(Metadata metadata, String stringKey, byte[] key) {
        KeyIndex keyIndexTrack = this.serviceContext.keyIndexService().lookup(metadata.source(),stringKey);
        if(keyIndexTrack==null) return null;
        return serviceContext.clusterProvider().accessIndexService().onRecover(metadata.partition(),key,nodeList(keyIndexTrack));
    }

    @Override
    public void onDeleting(Metadata metadata, byte[] key) {

    }



}
