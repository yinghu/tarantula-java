package com.tarantula.platform.service.persistence;

import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metadata;

import com.icodesoftware.util.BinaryKey;
import com.tarantula.platform.service.KeyIndexTrack;
import com.tarantula.platform.service.cluster.keyindex.DistributionKeyIndexService;

import java.nio.ByteBuffer;


public class IndexScopeReplicationProxy extends ScopedReplicationProxy {

    private TarantulaLogger logger = JDKLogger.getLogger(IndexScopeReplicationProxy.class);

    private DistributionKeyIndexService distributionKeyIndexService;

    public IndexScopeReplicationProxy(){
        super(Distributable.INDEX_SCOPE);
    }



    public void onDistributing(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value){
        //KeyIndexTrack keyIndexTrack = new KeyIndexTrack(metadata.source(),new BinaryKey(key.array()));
        //this.serviceContext.keyIndexService().createIfAbsent()
        logger.warn(metadata.source()+">"+metadata.label());
    }
    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer buffer){
        logger.warn("Recovering from "+metadata.source()+"]");
        byte[] data = distributionKeyIndexService.recover(metadata.source(),key.array());
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



    @Override
    public void waitForData() {
        this.distributionKeyIndexService = serviceContext.clusterProvider().serviceProvider(DistributionKeyIndexService.NAME);
        logger.warn("Index scope replication proxy is ready");
    }
}
