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
    public void setupTest() {
        RevisionObject toData = new RevisionObject("abc".getBytes(),100);
        byte[] data = toData.toBinary();
        RevisionObject fromData = new RevisionObject();
        fromData.fromBinary(data);
        Assert.assertEquals(true,fromData.revision()==100);
        Assert.assertEquals(true,new String(fromData.data).equals("abc"));
    }


}
