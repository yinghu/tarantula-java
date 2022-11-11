package com.tarantula.test;

import com.tarantula.platform.service.persistence.ClusterNode;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.LinkedList;

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

    }
}
