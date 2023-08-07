package com.tarantula.test;

import com.icodesoftware.service.ClusterProvider;
import com.tarantula.platform.service.persistence.ClusterNode;
import com.tarantula.platform.service.persistence.berkeley.ScopedReplicationProxy;
import org.testng.Assert;
import org.testng.annotations.Test;


public class ScopedReplicationProxyTest {


    @Test(groups = { "ScopedReplicationProxy" })
    public void atomicOperationTest() {
        ScopedReplicationProxy proxy = new ScopedReplicationProxy(null);
        ClusterProvider.Node node = proxy.nextNode();
        Assert.assertNull(node);
        ClusterNode node1 = new ClusterNode("BDS","N01",17);
        ClusterNode node2 = new ClusterNode("BDS","N02",17);
        ClusterNode node3 = new ClusterNode("BDS","N03",17);
        proxy.nodeRemoved(node1);
        proxy.nodeAdded(node1);
        proxy.nodeAdded(node2);
        proxy.nodeAdded(node3);
        proxy.nodeAdded(node1);
        proxy.nodeAdded(node2);
        proxy.nodeAdded(node3);
        for(int i=0;i<10;i++){
            ClusterProvider.Node n = proxy.nextNode();
            Assert.assertNotNull(n);
        }
        ClusterProvider.Node[] nlist = proxy.nextNodeList(3);
        ClusterProvider.Node[] nlist1 = proxy.nextNodeList(5);
        Assert.assertNotNull(nlist[0]);
        Assert.assertNotNull(nlist[1]);
        Assert.assertNotNull(nlist[2]);
        Assert.assertNotNull(nlist1[0]);
        Assert.assertNotNull(nlist1[1]);
        Assert.assertNotNull(nlist1[2]);
        Assert.assertNull(nlist1[3]);
        Assert.assertNull(nlist1[4]);
        proxy.nodeRemoved(node2);
        proxy.nodeRemoved(node3);
        for(int i=0;i<10;i++){
            ClusterProvider.Node n = proxy.nextNode();
            Assert.assertNotNull(n);
        }
        proxy.nodeRemoved(node1);
        for(int i=0;i<10;i++){
            ClusterProvider.Node n = proxy.nextNode();
            Assert.assertNull(n);
        }

    }

}
