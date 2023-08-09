package com.tarantula.platform.service.persistence.berkeley;

import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.service.Metadata;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.KeyIndexTrack;


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
        logger.warn(metadata.source()+"#"+stringKey);
        KeyIndex keyIndex = this.lookup(metadata.source(),stringKey);
        if(keyIndex==null){
            ClusterProvider.Node[] nodes = nextNodeList(serviceContext.clusterProvider().maxReplicationNumber());
            int replicated = this.serviceContext.clusterProvider().recoverService().onReplicate(metadata.source(),key,value,nodes);
            logger.warn("Replication number ["+replicated+"] of "+serviceContext.clusterProvider().maxReplicationNumber()+"]");
            if(replicated==0){
                keyIndex = new KeyIndexTrack();
                keyIndex.owner(metadata.source());
                keyIndex.index(stringKey);
                keyIndex.placeMasterNode(localNode.nodeName());
                this.serviceContext.keyIndexService().createIfAbsent(keyIndex);
            }
            return;
        }
        int replicated = this.serviceContext.clusterProvider().recoverService().onReplicate(metadata.source(),key,value,nodeList(keyIndex));
        logger.warn("Replication number ["+replicated+"] of "+serviceContext.clusterProvider().maxReplicationNumber()+"]");
    }



    @Override
    public byte[] onRecovering(Metadata metadata, String stringKey, byte[] key) {
        KeyIndex keyIndexTrack = this.lookup(metadata.source(),stringKey);
        if(keyIndexTrack==null){
            //if(metadata.source().equals("tarantula_user")){
            logger.warn("kindex null->"+metadata.source()+"#"+stringKey);
            //}
            return null;
        }
        if(metadata.source().equals("tarantula_user")){
            logger.warn("kindex->"+keyIndexTrack.masterNode());
        }
        return serviceContext.clusterProvider().recoverService().onRecover(metadata.source(),key,nodeList(keyIndexTrack));
    }

    @Override
    public void onDeleting(Metadata metadata, byte[] key) {

    }


}
