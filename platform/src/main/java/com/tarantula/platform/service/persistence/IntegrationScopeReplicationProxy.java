package com.tarantula.platform.service.persistence;

import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.icodesoftware.util.BinaryKey;
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
        ClusterProvider.Node[] nlist = nextNodeList(maxReplicationNumber());
        int replicated = this.serviceContext.clusterProvider().accessIndexService().onReplicate(localNode.nodeName(),binaryKey.key,value.array(),nlist);
        logger.warn("Distributing ["+metadata.source()+"]["+replicated+"]["+nlist.length+"]");
        if(replicated>0) return;
        logger.warn("Replication number [" + replicated + "] of " + serviceContext.clusterProvider().maxReplicationNumber() + "]");
        KeyIndex keyIndex = new KeyIndexTrack(metadata.source(),binaryKey);
        keyIndex.placeMasterNode(localNode.nodeName());
        if(!this.serviceContext.keyIndexService().createIfAbsent(keyIndex)){
            keyIndex.placeMasterNode(localNode.nodeName());
            this.serviceContext.keyIndexService().update(keyIndex);
        }
    }
    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer buffer){
        BinaryKey binaryKey = new BinaryKey(key.array());
        KeyIndex keyIndex = serviceContext.keyIndexService().lookup(metadata.label()==null?metadata.source():metadata.source()+"_"+metadata.label(),binaryKey);
        if(keyIndex==null) return false;
        ClusterProvider.Node[] nlist = nodeList(keyIndex);
        logger.warn("Recovering DB : "+metadata.source()+" Node Size : "+nlist.length);
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

}
