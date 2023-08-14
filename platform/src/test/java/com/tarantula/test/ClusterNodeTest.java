package com.tarantula.test;

import com.icodesoftware.service.ClusterProvider;
import com.tarantula.platform.service.persistence.ClusterNode;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class ClusterNodeTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "ClusterNode" })
    public void localTest() {
        LinkedList<ClusterNode> queue = new LinkedList<>();
        for(int i=0;i<10;i++){
            queue.offer(new ClusterNode("m"+i));
        }
        ClusterNode c = queue.poll();
        Assert.assertEquals(true,c.memberId.equals("m0"));
        queue.remove(new ClusterNode("m1"));
        queue.offer(c);
        queue.remove(c);
        queue.poll();
        queue.poll();
        Assert.assertEquals(true,queue.size()==6);

        ConcurrentHashMap<ClusterProvider.Node,String> mix = new ConcurrentHashMap<>();
        ClusterProvider.Node mnode = new ClusterNode("mid");
        ClusterProvider.Node nnode = new ClusterNode("","N05",5);
        mix.put(mnode,"memid");
        mix.put(nnode,"nnid");
        Assert.assertEquals(mix.remove(mnode),"memid");
        Assert.assertEquals(mix.remove(nnode),"nnid");
    }
}
