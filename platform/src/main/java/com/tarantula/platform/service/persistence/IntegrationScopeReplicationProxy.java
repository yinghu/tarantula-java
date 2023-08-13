package com.tarantula.platform.service.persistence;

import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.tarantula.platform.event.IntegrationReplicationEvent;
import com.tarantula.platform.service.KeyIndexTrack;

import java.util.ArrayList;

public class IntegrationScopeReplicationProxy extends ScopedReplicationProxy {

    private TarantulaLogger logger = JDKLogger.getLogger(IntegrationScopeReplicationProxy.class);

    public IntegrationScopeReplicationProxy(){
        super(Distributable.INTEGRATION_SCOPE);
    }
    @Override
    public <T extends Recoverable> void onBackingUp(Metadata metadata, String key, T t) {

    }
    @Override
    public void onDistributing(Metadata metadata, String stringKey, byte[] key, byte[] value) {
        if(asyncDistributing){
            ClusterProvider.Node[] nodes = nextNodeList(serviceContext.clusterProvider().maxReplicationNumber());
            for(ClusterProvider.Node node : nodes){
                OffHeapIntegrationScopeReplication offHeapOnReplication = new OffHeapIntegrationScopeReplication();
                offHeapOnReplication.write(node,metadata.partition(),key,value);
                if(!pendingReplication.offer(offHeapOnReplication)){
                    offHeapOnReplication.drop();
                    replicate();
                }
            }
            return;
        }
        ClusterProvider.Node[] nodes = nextNodeList(serviceContext.clusterProvider().maxReplicationNumber());
        int replicated = this.serviceContext.clusterProvider().accessIndexService().onReplicate(localNode.nodeName(),metadata.partition(),key,value,nodes);
        if(replicated==0) {
            logger.warn("Replication number [" + replicated + "] of " + serviceContext.clusterProvider().maxReplicationNumber() + "]");
            KeyIndex keyIndex = new KeyIndexTrack();
            keyIndex.owner(metadata.source());
            keyIndex.index(stringKey);
            keyIndex.placeMasterNode(localNode.nodeName());
            this.serviceContext.keyIndexService().createIfAbsent(keyIndex);
        }
    }


    @Override
    public byte[] onRecovering(Metadata metadata, String stringKey, byte[] key) {
        KeyIndex keyIndexTrack = this.lookup(metadata.source(),stringKey);
        if(keyIndexTrack==null) return null;
        return serviceContext.clusterProvider().accessIndexService().onRecover(metadata.partition(),key,nodeList(keyIndexTrack));
    }

    @Override
    public void onDeleting(Metadata metadata, byte[] key) {

    }

    protected void replicate(){
        ArrayList<ScopedOnReplication> drop = new ArrayList<>();
        pendingReplication.drainTo(drop,maxBatchSize);
        drop.forEach(d->{
            OnReplication onReplication = d.read();
            reusingReplication.offer(d);
            //serviceContext.clusterProvider().accessIndexService().onReplicate(onReplication.nodeName(),onReplication.partition(),onReplication.key(),onReplication.value())
        });
    }

}
