package com.tarantula.platform.service.persistence.berkeley;

import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.service.Metadata;
import com.tarantula.platform.service.DataStoreProvider;


public class DataScopeReplicationProxy extends ScopedReplicationProxy {
    private TarantulaLogger logger = JDKLogger.getLogger(DataScopeReplicationProxy.class);

    public DataScopeReplicationProxy(DataStoreProvider dataStoreProvider){
        super(dataStoreProvider);
    }
    @Override
    public <T extends Recoverable> void onBackingUp(Metadata metadata, String key, T t) {

    }

    @Override
    public void onDistributing(Metadata metadata, String stringKey, byte[] key, byte[] value) {
        KeyIndex keyIndex = this.serviceContext.keyIndexService().lookup(metadata.source(),stringKey);
        if(keyIndex==null){
            //logger.warn("Index Key->"+metadata.source()+"#"+stringKey);
            ClusterProvider.Node[] nodes = nextNodeList(serviceContext.clusterProvider().maxReplicationNumber());
            int replicated = this.serviceContext.clusterProvider().recoverService().onReplicate(metadata.source(),key,value,nodes);
            //logger.warn("Replication number ["+replicated+"] of "+serviceContext.clusterProvider().maxReplicationNumber()+"]");
        }
        else{

        }
    }



    @Override
    public byte[] onRecovering(Metadata metadata, String stringKey, byte[] key) {
        //logger.warn("recovering from key->"+metadata.source()+"#"+stringKey);
        KeyIndex keyIndexTrack = this.serviceContext.keyIndexService().lookup(metadata.source(),stringKey);
        if(keyIndexTrack==null) return null;
        return serviceContext.clusterProvider().recoverService().onRecover(metadata.source(),key);
    }

    @Override
    public void onDeleting(Metadata metadata, byte[] key) {

    }


}
