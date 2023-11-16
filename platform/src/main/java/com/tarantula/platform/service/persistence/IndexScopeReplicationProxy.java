package com.tarantula.platform.service.persistence;

import com.icodesoftware.Distributable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;


import com.tarantula.platform.service.cluster.keyindex.DistributionKeyIndexService;


public class IndexScopeReplicationProxy extends ScopedReplicationProxy {

    private TarantulaLogger logger = JDKLogger.getLogger(IndexScopeReplicationProxy.class);

    private DistributionKeyIndexService distributionKeyIndexService;

    public IndexScopeReplicationProxy(){
        super(Distributable.INDEX_SCOPE);
    }


    @Override
    public void waitForData() {
        this.distributionKeyIndexService = serviceContext.clusterProvider().serviceProvider(DistributionKeyIndexService.NAME);
        logger.warn("Index scope replication proxy is ready");
    }
}
