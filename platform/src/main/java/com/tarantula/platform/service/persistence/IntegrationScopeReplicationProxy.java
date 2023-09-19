package com.tarantula.platform.service.persistence;

import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.icodesoftware.util.BinaryKey;
import com.tarantula.platform.event.IntegrationReplicationEvent;
import com.tarantula.platform.service.KeyIndexTrack;

public class IntegrationScopeReplicationProxy extends ScopedReplicationProxy {

    private TarantulaLogger logger = JDKLogger.getLogger(IntegrationScopeReplicationProxy.class);

    public IntegrationScopeReplicationProxy(){
        super(Distributable.INTEGRATION_SCOPE);
    }


    public void onDistributing(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value){
        if(asyncDistributing){
            return;
        }
        BinaryKey binaryKey = new BinaryKey(key.array());
        KeyIndexTrack keyIndexTrack = new KeyIndexTrack(metadata.source(),binaryKey);
        keyIndexTrack.placeMasterNode(localNode.nodeName());
        if(!this.serviceContext.keyIndexService().createIfAbsent(keyIndexTrack)){
            keyIndexTrack.placeMasterNode(localNode.nodeName());
            this.serviceContext.keyIndexService().update(keyIndexTrack);
        }
        ClusterProvider.Node[] nlist = nextNodeList(3);
        int c = this.serviceContext.clusterProvider().accessIndexService().onReplicate(localNode.nodeName(),binaryKey.key,value.array(),nlist);
        logger.warn("Distributing ["+metadata.source()+"]["+c+"]["+nlist.length+"]");
    }
    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer buffer){
        BinaryKey binaryKey = new BinaryKey(key.array());
        KeyIndex keyIndex = serviceContext.keyIndexService().lookup(metadata.source(),binaryKey);
        if(keyIndex==null) return false;
        byte[] data = serviceContext.clusterProvider().accessIndexService().onRecover(metadata.source(),binaryKey.key,nodeList(keyIndex));
        if(data==null) return false;
        for(byte b : data){
            buffer.writeByte(b);
        }
        return true;
    }
    @Override
    public void onDeleting(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value) {
        //this.serviceContext.clusterProvider().recoverService().onDelete(metadata.source(),key);
    }
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
                        e = new IntegrationReplicationEvent(maxPendingSize, localNode.nodeName(), node.nodeName());
                        serviceContext.schedule(new ReplicationSynchronizerTimeout(this,syncInterval,n));
                    }
                    OffHeapIntegrationScopeReplication offHeapOnReplication = new OffHeapIntegrationScopeReplication();
                    offHeapOnReplication.write(node.nodeName(),metadata.partition(),key,value);
                    if(e.offer(offHeapOnReplication)) return e;
                    serviceContext.schedule(new ReplicationSynchronizerOverflow(serviceContext,OVERFLOW_TIMER,e));
                    IntegrationReplicationEvent ex = new IntegrationReplicationEvent(maxPendingSize,localNode.nodeName(),node.nodeName());
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
                return;
            }
            return;
        }
        ClusterProvider.Node[] nodes = nextNodeList(serviceContext.clusterProvider().maxReplicationNumber());
        int replicated = this.serviceContext.clusterProvider().accessIndexService().onReplicate(localNode.nodeName(),key,value,nodes);
        if(replicated==0) {
            logger.warn("Replication number [" + replicated + "] of " + serviceContext.clusterProvider().maxReplicationNumber() + "]");
            KeyIndex keyIndex = new KeyIndexTrack();
            keyIndex.owner(metadata.source());
            keyIndex.index(stringKey);
            keyIndex.placeMasterNode(localNode.nodeName());
            this.serviceContext.keyIndexService().createIfAbsent(keyIndex);
        }
    }



    public byte[] onRecovering(Metadata metadata, String stringKey, byte[] key) {
        KeyIndex keyIndexTrack = this.lookup(metadata.source(),new BinaryKey(key));
        if(keyIndexTrack==null) return null;
        return serviceContext.clusterProvider().accessIndexService().onRecover(metadata.source(),key,nodeList(keyIndexTrack));
    }



}
