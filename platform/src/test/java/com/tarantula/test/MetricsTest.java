package com.tarantula.test;


import com.tarantula.platform.service.metrics.SystemStatistics;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MetricsTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "PerformanceMetrics" })
    public void localTest() {
        EmptyDataStore emptyDataStore = new EmptyDataStore();
        SystemStatistics systemStatistics = new SystemStatistics();
        
        //byte[] data = RevisionObject.toBinary(100,"abc".getBytes(),true);
        //RevisionObject fromData =  RevisionObject.fromBinary(data);
        //Assert.assertEquals(fromData.revision == 100,true);
        //Assert.assertEquals(new String(fromData.data).equals("abc"),true);
        //Assert.assertEquals(fromData.local,true);
    }


}
