package com.tarantula.test;

import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.service.persistence.RevisionObject;
import com.tarantula.platform.util.SystemUtil;
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

    @Test(groups = { "RevisionObject" })
    public void recoverableTest() {
        byte[] node ="n01".getBytes();
        AccessIndexTrack accessIndexTrack = new AccessIndexTrack("name","DBS", SystemUtil.oid(),1);
        RevisionObject revisionObject = RevisionObject.fromRecoverable(accessIndexTrack,node);
        RevisionObject copy = RevisionObject.fromBinary(revisionObject.toBinary());
        Assert.assertEquals(revisionObject.data,copy.data);
        Assert.assertEquals(revisionObject.revision,copy.revision);
        Assert.assertEquals(revisionObject.local,copy.local);
        Assert.assertEquals(revisionObject.node,copy.node);
        AccessIndexTrack ro = new AccessIndexTrack();
        ro.fromBinary(copy.data);
        Assert.assertEquals(ro.bucket(),"DBS");
        Assert.assertEquals(ro.oid(),accessIndexTrack.oid());
        Assert.assertEquals(ro.referenceId(),accessIndexTrack.referenceId());
    }
}
