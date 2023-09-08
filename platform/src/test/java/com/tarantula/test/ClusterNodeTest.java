package com.tarantula.test;

import com.icodesoftware.AccessIndex;
import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.AccessIndexService;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.service.persistence.ClusterNode;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ClusterNodeTest extends DataStoreHook{

    @Test(groups = { "ClusterNode" })
    public void clusterNodeSetupTest() {
        DataStore dataStore = dataStoreProvider.createAccessIndexDataStore(AccessIndexService.AccessIndexStore.STORE_NAME);
        ClusterNode node = (ClusterNode)serviceContext.node();
        long bucketId = serviceContext.distributionId();
        long nodeId = serviceContext.distributionId();
        long developmentId = serviceContext.distributionId();
        AccessIndexTrack abucket = new AccessIndexTrack(node.clusterNameSuffix()+ Recoverable.PATH_SEPARATOR+node.bucketName(),AccessIndex.SYSTEM_INDEX,bucketId);
        AccessIndexTrack anode = new AccessIndexTrack(node.clusterNameSuffix()+ Recoverable.PATH_SEPARATOR+node.nodeName(),AccessIndex.SYSTEM_INDEX,nodeId);
        AccessIndexTrack adevelop = new AccessIndexTrack(node.clusterNameSuffix()+ Recoverable.PATH_SEPARATOR+"development",AccessIndex.SYSTEM_INDEX,developmentId);
        Assert.assertTrue(dataStore.createIfAbsent(abucket,false));
        Assert.assertTrue(dataStore.createIfAbsent(anode,false));
        Assert.assertTrue(dataStore.createIfAbsent(adevelop,false));
        node.bucketId = abucket.distributionId();
        node.nodeId = anode.distributionId();
        node.deploymentId = adevelop.distributionId();
        Assert.assertEquals(node.bucketId(),bucketId);
        Assert.assertEquals(node.nodeId(),nodeId);
        Assert.assertEquals(node.deploymentId(),developmentId);
    }
}
