package com.tarantula.platform.service.persistence.berkeley;

import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.service.Metadata;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.persistence.RevisionObject;


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

    }

    @Override
    public void onDistributing(Metadata metadata, String stringKey, byte[] key, RevisionObject value) {
        KeyIndex keyIndex = this.serviceContext.keyIndexService().lookup(metadata.source(),stringKey);
        if(keyIndex==null){
            ClusterProvider.Node[] nodes = nextNodeList(serviceContext.clusterProvider().maxReplicationNumber());
            int expected = this.serviceContext.clusterProvider().accessIndexService().onReplicate(metadata.partition(),key,value.toBinary(),nodes);
            logger.warn("Replication number ->"+expected);
        }
        else{

        }
    }

    @Override
    public byte[] onRecovering(Metadata metadata, String stringKey, byte[] key) {
        KeyIndex keyIndexTrack = this.serviceContext.keyIndexService().lookup(metadata.source(),stringKey);
        if(keyIndexTrack==null) return null;
        return serviceContext.clusterProvider().accessIndexService().onRecover(metadata.partition(),key);
    }

    @Override
    public void onDeleting(Metadata metadata, byte[] key) {

    }



}
