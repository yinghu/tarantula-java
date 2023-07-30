package com.tarantula.test;

import com.tarantula.platform.service.persistence.RevisionObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RevisionObjectTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "RevisionObject" })
    public void localTest() {
        byte[] node ="n01".getBytes();
        byte[] data = RevisionObject.toBinary(100,"abc".getBytes(),true,node);
        RevisionObject fromData =  RevisionObject.fromBinary(data);
        Assert.assertEquals(fromData.revision == 100,true);
        Assert.assertEquals(new String(fromData.data).equals("abc"),true);
        Assert.assertEquals(fromData.local,true);
        Assert.assertEquals(fromData.node,node);
    }

    @Test(groups = { "RevisionObject" })
    public void remoteTest() {
        byte[] node ="n01".getBytes();
        byte[] data = RevisionObject.toBinary(100,"abc".getBytes(),false,node);
        RevisionObject fromData =  RevisionObject.fromBinary(data);
        Assert.assertEquals(fromData.revision == 100,true);
        Assert.assertEquals(new String(fromData.data).equals("abc"),true);
        Assert.assertEquals(fromData.local,false);
        Assert.assertEquals(fromData.node,node);
    }
}
