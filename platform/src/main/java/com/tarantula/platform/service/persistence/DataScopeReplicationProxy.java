package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.icodesoftware.util.BinaryKey;
import com.tarantula.platform.service.KeyIndexTrack;

public class DataScopeReplicationProxy extends ScopedReplicationProxy {
    private TarantulaLogger logger = JDKLogger.getLogger(DataScopeReplicationProxy.class);
    public DataScopeReplicationProxy(){
        super(Distributable.DATA_SCOPE);
    }


    public void onUpdating(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId){
        if(asyncDistributing){
            return;
        }
        BinaryKey binaryKey = new BinaryKey(key.array());
        ClusterProvider.Node[] nlist = nextNodeList(maxReplicationNumber());
        int replicated = this.serviceContext.clusterProvider().recoverService().onReplicate(localNode.nodeName(),metadata.source(),metadata.label(),binaryKey.key,value.array(),nlist);
        logger.warn("Distributing DB ["+metadata.source()+"] Replicated ["+replicated+"] Node Size ["+nlist.length+"]");
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
        logger.warn("Recovering DB : "+metadata.source()+" LABEL : "+metadata.label());
        BinaryKey binaryKey = new BinaryKey(key.array());
        KeyIndex keyIndex = serviceContext.keyIndexService().lookup(metadata.label()==null?metadata.source(): metadata.source()+"_"+metadata.label(),binaryKey);
        if(keyIndex==null) return false;
        ClusterProvider.Node[] nlist = nodeList(keyIndex);
        if(metadata.label()==null){
            logger.warn("Recovering DB : "+metadata.source()+" Node Size :"+nlist.length);
            byte[] data = serviceContext.clusterProvider().recoverService().onRecover(metadata.source(),binaryKey.key,nodeList(keyIndex));
            if(data==null) return false;
            for(byte b : data){
                buffer.writeByte(b);
            }
            return true;
        }else{
            logger.warn("Recovering EDGE : "+metadata.source()+" : "+metadata.label()+" : "+nlist.length);
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
    public boolean onDeleting(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId) {
        logger.warn("DB Delete : "+metadata.source()+" : "+metadata.label());
        RecoverService recoverService = this.serviceContext.clusterProvider().recoverService();
        if(metadata.label()==null) {
            return recoverService.onDelete(metadata.source(),key.array());
        }
        if(value==null){
            return recoverService.onDeleteEdge(metadata.source(),metadata.label(),key.array());
        }
        return recoverService.onDeleteEdge(metadata.source(),metadata.label(),key.array(),value.array());
    }

}
