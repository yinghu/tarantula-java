package com.tarantula.platform.service.persistence;

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
    @Override
    public <T extends Recoverable> void onBackingUp(Metadata metadata, String key, T t) {

    }


    public void onDistributing(Metadata metadata, String stringKey, byte[] key, byte[] value) {

    }



    @Override
    public byte[] onRecovering(Metadata metadata, String stringKey, byte[] key) {
        return this.distributionKeyIndexService.recover(metadata.partition(),key);
    }

    @Override
    public void onDeleting(Metadata metadata, byte[] key) {

    }

    @Override
    public void waitForData() {
        this.distributionKeyIndexService = serviceContext.clusterProvider().serviceProvider(DistributionKeyIndexService.NAME);
        logger.warn("Index scope replication proxy is ready");
    }
}
