package com.tarantula.test;

import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.KeyIndex;
import com.tarantula.platform.service.KeyIndexTrack;
import com.tarantula.platform.service.cluster.keyindex.ClusterNodeManager;
import com.tarantula.platform.service.persistence.ClusterNode;
import com.tarantula.platform.service.persistence.berkeley.ScopedReplicationProxy;
import org.testng.Assert;
import org.testng.annotations.Test;


public class ClusterNodeManagerTest {


    @Test(groups = { "ScopedReplicationProxy" })
    public void atomicOperationTest() {
        ClusterNodeManager proxy = new ClusterNodeManager(new ClusterNode("","n12",17));
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

    @Test(groups = { "ScopedReplicationProxy" })
    public void keyIndexRecoverOperationTest() {
        ClusterNodeManager proxy = new ClusterNodeManager(new ClusterNode("","N09",17));
        for(int i=0;i<10;i++) {
            ClusterNode node1 = new ClusterNode("BDS", "N0"+i, 17);
            proxy.nodeAdded(node1);
        }
        for(int i=0;i<10;i++){
            Assert.assertEquals(proxy.pendingNodes()[i].nodeName(),"N0"+(9-i));
        }
        KeyIndex keyIndex = new KeyIndexTrack();
        keyIndex.placeMasterNode("N06");
        Assert.assertEquals(keyIndex.masterNode(),"N06");

        keyIndex.placeSlaveNode("N01");
        Assert.assertEquals(keyIndex.slaveNodes().length,1);
        Assert.assertEquals(keyIndex.slaveNodes()[0],"N01");

        keyIndex.placeSlaveNode("N03");
        Assert.assertEquals(keyIndex.slaveNodes().length,2);
        Assert.assertEquals(keyIndex.slaveNodes()[0],"N03");
        Assert.assertEquals(keyIndex.slaveNodes()[1],"N01");

        keyIndex.placeSlaveNode("N07");
        Assert.assertEquals(keyIndex.slaveNodes().length,3);
        Assert.assertEquals(keyIndex.slaveNodes()[0],"N07");
        Assert.assertEquals(keyIndex.slaveNodes()[1],"N03");
        Assert.assertEquals(keyIndex.slaveNodes()[2],"N01");

        keyIndex.placeSlaveNode("N08");
        Assert.assertEquals(keyIndex.slaveNodes().length,4);
        Assert.assertEquals(keyIndex.slaveNodes()[0],"N08");
        Assert.assertEquals(keyIndex.slaveNodes()[1],"N07");
        Assert.assertEquals(keyIndex.slaveNodes()[2],"N03");
        Assert.assertEquals(keyIndex.slaveNodes()[3],"N01");

        verify(proxy.nodeList(keyIndex));

    }

    @Test(groups = { "ScopedReplicationProxy" })
    public void keyIndexReplicateOperationTest() {
        ClusterNodeManager proxy = new ClusterNodeManager(new ClusterNode("","N09",17));
        for(int i=0;i<10;i++) {
            ClusterNode node1 = new ClusterNode("BDS", "N0"+i, 17);
            proxy.nodeAdded(node1);
        }
        for(int i=0;i<10;i++){
            Assert.assertEquals(proxy.pendingNodes()[i].nodeName(),"N0"+(9-i));
        }
        KeyIndex keyIndex = new KeyIndexTrack();
        keyIndex.placeMasterNode("N06");
        Assert.assertEquals(keyIndex.masterNode(),"N06");
        verify(proxy.nodeList(keyIndex,3));

        keyIndex.placeSlaveNode("N01");
        Assert.assertEquals(keyIndex.slaveNodes().length,1);
        Assert.assertEquals(keyIndex.slaveNodes()[0],"N01");

        keyIndex.placeSlaveNode("N03");
        Assert.assertEquals(keyIndex.slaveNodes().length,2);
        Assert.assertEquals(keyIndex.slaveNodes()[0],"N03");
        Assert.assertEquals(keyIndex.slaveNodes()[1],"N01");

        keyIndex.placeSlaveNode("N07");
        Assert.assertEquals(keyIndex.slaveNodes().length,3);
        Assert.assertEquals(keyIndex.slaveNodes()[0],"N07");
        Assert.assertEquals(keyIndex.slaveNodes()[1],"N03");
        Assert.assertEquals(keyIndex.slaveNodes()[2],"N01");

        keyIndex.placeSlaveNode("N08");
        Assert.assertEquals(keyIndex.slaveNodes().length,4);
        Assert.assertEquals(keyIndex.slaveNodes()[0],"N08");
        Assert.assertEquals(keyIndex.slaveNodes()[1],"N07");
        Assert.assertEquals(keyIndex.slaveNodes()[2],"N03");
        Assert.assertEquals(keyIndex.slaveNodes()[3],"N01");

        verify(proxy.nodeList(keyIndex,3));

    }

    private void verify(ClusterProvider.Node[] nodes){
        for(ClusterProvider.Node n : nodes){
            Assert.assertNotNull(n);
        }
    }
}
