package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Batchable;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.KeyIndex;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.BinaryKey;
import com.tarantula.platform.service.KeyIndexTrack;

public class DataScopeReplicationProxy extends ScopedReplicationProxy {
    private TarantulaLogger logger = JDKLogger.getLogger(DataScopeReplicationProxy.class);

    public DataScopeReplicationProxy(){
        super(Distributable.DATA_SCOPE);
    }

    public void onDistributing(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value){
        if(asyncDistributing){
            return;
        }
        BinaryKey binaryKey = new BinaryKey(key.array());
        ClusterProvider.Node[] nlist = nextNodeList(maxReplicationNumber());
        int replicated = this.serviceContext.clusterProvider().recoverService().onReplicate(localNode.nodeName(),metadata.source(),metadata.label(),binaryKey.key,value.array(),nlist);
        logger.warn("Distributing ["+metadata.source()+"]["+replicated+"]["+nlist.length+"]");
        if(replicated>0) return;
        logger.warn("Replication number [" + replicated + "] of " + serviceContext.clusterProvider().maxReplicationNumber() + "]");
        KeyIndex keyIndex = new KeyIndexTrack(metadata.label()==null?metadata.source():metadata.source()+"_"+metadata.label(),binaryKey);
        keyIndex.placeMasterNode(localNode.nodeName());
        if(!this.serviceContext.keyIndexService().createIfAbsent(keyIndex)){
            keyIndex.placeMasterNode(localNode.nodeName());
            this.serviceContext.keyIndexService().update(keyIndex);
        }
    }
    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer buffer){
        logger.warn("Recovering ["+metadata.source()+":"+metadata.label());
        BinaryKey binaryKey = new BinaryKey(key.array());
        KeyIndex keyIndex = serviceContext.keyIndexService().lookup(metadata.label()==null?metadata.source(): metadata.source()+"_"+metadata.label(),binaryKey);
        if(keyIndex==null) return false;
        ClusterProvider.Node[] nlist = nodeList(keyIndex);
        if(metadata.label()==null){
            logger.warn("Recovering ["+metadata.source()+"]["+nlist.length+"]");
            byte[] data = serviceContext.clusterProvider().recoverService().onRecover(metadata.source(),binaryKey.key,nodeList(keyIndex));
            if(data==null) return false;
            for(byte b : data){
                buffer.writeByte(b);
            }
            return true;
        }else{
            logger.warn("Recovering edge ["+metadata.source()+":"+metadata.label()+"]["+nlist.length+"]");
            Batchable batch = serviceContext.clusterProvider().recoverService().onRecover(metadata.source(),metadata.label(),binaryKey.key,nodeList(keyIndex));
            if(batch==null) return false;
            DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,metadata.source());
            batch.data().forEach(edge-> {
                dataStore.backup().setEdge(metadata.label(), (k, v) -> {
                    for (byte b : binaryKey.key) {
                        k.writeByte(b);
                    }
                    for (byte b : edge) {
                        v.writeByte(b);
                    }
                    return true;
                });
                byte[] data = this.serviceContext.clusterProvider().recoverService().onRecover(metadata.source(),edge,nlist);
                dataStore.backup().set((k,v)->{
                    for (byte b : edge) {
                        k.writeByte(b);
                    }
                    for (byte b : data) {
                        v.writeByte(b);
                    }
                    return true;
                });

            });

            return true;
        }
    }
    @Override
    public void onDeleting(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value) {
        //this.serviceContext.clusterProvider().recoverService().onDelete(metadata.source(),key);
    }

}
