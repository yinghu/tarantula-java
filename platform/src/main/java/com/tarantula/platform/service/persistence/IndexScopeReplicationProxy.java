package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metadata;

import com.tarantula.platform.service.cluster.keyindex.DistributionKeyIndexService;


public class IndexScopeReplicationProxy extends ScopedReplicationProxy {

    private TarantulaLogger logger = JDKLogger.getLogger(IndexScopeReplicationProxy.class);

    private DistributionKeyIndexService distributionKeyIndexService;

    public IndexScopeReplicationProxy(){
        super(Distributable.INDEX_SCOPE);
    }



    public void onUpdating(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId){

    }
    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer buffer, DataStore.BufferStream bufferStream){
        logger.warn("Recovering DB : "+metadata.source()+" Label : "+metadata.label());
        byte[] data = distributionKeyIndexService.recover(metadata.source(),key.array());
        if(data==null) return false;
        for(byte b : data){
            buffer.writeByte(b);
        }
        return true;
    }

    public boolean onRecovering(Metadata metadata,Recoverable.DataBuffer key,DataStore.BufferEdgeStream bufferStream){
        return false;
    }
    @Override
    public void waitForData() {
        this.distributionKeyIndexService = serviceContext.clusterProvider().serviceProvider(DistributionKeyIndexService.NAME);
        logger.warn("Index scope replication proxy is ready");
    }
}
