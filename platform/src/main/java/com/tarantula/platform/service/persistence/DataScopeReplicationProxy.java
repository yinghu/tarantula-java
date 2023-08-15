package com.tarantula.platform.service.persistence;

import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.service.Metadata;
import com.tarantula.platform.event.DataReplicationEvent;
import com.tarantula.platform.service.KeyIndexTrack;


public class DataScopeReplicationProxy extends ScopedReplicationProxy {
    private TarantulaLogger logger = JDKLogger.getLogger(DataScopeReplicationProxy.class);

    public DataScopeReplicationProxy(){
        super(Distributable.DATA_SCOPE);
    }
    @Override
    public <T extends Recoverable> void onBackingUp(Metadata metadata, String key, T t) {

    }

    @Override
    public void onDistributing(Metadata metadata, String stringKey, byte[] key, byte[] value) {
        if(asyncDistributing){
            ClusterProvider.Node[] nodes = nextNodeList(serviceContext.clusterProvider().maxReplicationNumber());
            int replicated = 0;
            for(ClusterProvider.Node node : nodes){
                if(node==null){
                    continue;
                }
                replicated++;
                pendingEvents.compute(node,(n,e)->{
                    if(e==null) {
                        e = new DataReplicationEvent(maxPendingSize, localNode.nodeName(), node.nodeName());
                        serviceContext.schedule(new ReplicationSynchronizerTimeout(this,syncInterval,n));
                    }
                    OffHeapDataScopeReplication offHeapOnReplication = new OffHeapDataScopeReplication();
                    offHeapOnReplication.write(node.nodeName(),metadata.source(),key,value);
                    if(e.offer(offHeapOnReplication)) return e;
                    serviceContext.schedule(new ReplicationSynchronizerOverflow(serviceContext,OVERFLOW_TIMER,e));
                    DataReplicationEvent ex = new DataReplicationEvent(maxPendingSize,localNode.nodeName(),node.nodeName());
                    ex.offer(offHeapOnReplication);
                    serviceContext.schedule(new ReplicationSynchronizerTimeout(this,syncInterval,n));
                    return ex;
                });
            }
            if(replicated==0){
                logger.warn("Replication number [" + 0 + "] of " + serviceContext.clusterProvider().maxReplicationNumber() + "]");
                KeyIndex keyIndex = new KeyIndexTrack();
                keyIndex.owner(metadata.source());
                keyIndex.index(stringKey);
                keyIndex.placeMasterNode(localNode.nodeName());
                this.serviceContext.keyIndexService().createIfAbsent(keyIndex);
            }
            return;
        }
        KeyIndex keyIndex = this.lookup(metadata.source(),stringKey);
        if(keyIndex==null){
            ClusterProvider.Node[] nodes = nextNodeList(this.maxReplicationNumber());
            int replicated = this.serviceContext.clusterProvider().recoverService().onReplicate(localNode.nodeName(),metadata.source(),key,value,nodes);
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
        int replicated = this.serviceContext.clusterProvider().recoverService().onReplicate(localNode.nodeName(),metadata.source(),key,value,nodeList(keyIndex,this.maxReplicationNumber()));
        logger.warn("Replication number ["+replicated+"] of "+serviceContext.clusterProvider().maxReplicationNumber()+"]");
    }

    @Override
    public byte[] onRecovering(Metadata metadata, String stringKey, byte[] key) {
        KeyIndex keyIndexTrack = this.lookup(metadata.source(),stringKey);
        if(keyIndexTrack==null) return null;
        return serviceContext.clusterProvider().recoverService().onRecover(metadata.source(),key,nodeList(keyIndexTrack));
    }

    @Override
    public void onDeleting(Metadata metadata, byte[] key) {

    }


}
