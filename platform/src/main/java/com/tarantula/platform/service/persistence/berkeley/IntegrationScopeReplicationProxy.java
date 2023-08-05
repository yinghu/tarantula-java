package com.tarantula.platform.service.persistence.berkeley;

import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.service.persistence.MapStoreListener;
import com.tarantula.platform.service.persistence.RevisionObject;

public class IntegrationScopeReplicationProxy implements MapStoreListener, ServiceProvider, ClusterProvider.NodeListener {

    private TarantulaLogger logger = JDKLogger.getLogger(IntegrationScopeReplicationProxy.class);
    private ServiceContext serviceContext;

    @Override
    public <T extends Recoverable> void onBackingUp(Metadata metadata, String key, T t) {

    }

    @Override
    public void onDistributing(Metadata metadata, String stringKey, byte[] key, byte[] value) {

    }

    @Override
    public void onDistributing(Metadata metadata, String stringKey, byte[] key, RevisionObject value) {
        logger.warn("distributing ["+stringKey+"]");
    }

    @Override
    public byte[] onRecovering(Metadata metadata, byte[] key) {
        return null;
    }

    @Override
    public void onDeleting(Metadata metadata, byte[] key) {

    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        serviceContext.clusterProvider().registerNodeListener(this);
    }


    @Override
    public String name() {
        return null;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void nodeAdded(ClusterProvider.Node node) {
        logger.warn("Node added>"+node.nodeName()+">>"+node.memberId());
    }

    @Override
    public void nodeRemoved(ClusterProvider.Node node) {
        logger.warn("Node removed>"+node.nodeName());
    }
}
