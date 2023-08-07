package com.tarantula.platform.service.persistence.berkeley;

import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Metadata;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.cluster.keyindex.DistributionKeyIndexService;
import com.tarantula.platform.service.persistence.RevisionObject;

public class LocalScopeReplicationProxy extends ScopedReplicationProxy {

    private TarantulaLogger logger = JDKLogger.getLogger(LocalScopeReplicationProxy.class);

    private DistributionKeyIndexService distributionKeyIndexService;

    public LocalScopeReplicationProxy(DataStoreProvider dataStoreProvider){
        super(dataStoreProvider);
    }
    @Override
    public <T extends Recoverable> void onBackingUp(Metadata metadata, String key, T t) {

    }

    @Override
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
        logger.warn("Local scope replication proxy is ready");
    }
}
