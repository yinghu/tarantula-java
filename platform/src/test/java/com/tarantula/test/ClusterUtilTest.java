package com.tarantula.test;

import com.tarantula.platform.service.cluster.ClusterUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class ClusterUtilTest {


    @Test(groups = { "ClusterUtil" })
    public void callSuccessTest() {
        int[] t = {0};
        ClusterUtil.CallResult callResult = ClusterUtil.call(3,100,()->{
                t[0]++;
                if(t[0]<2) throw new IllegalArgumentException("less 2");
                return t[0];
        });
        Assert.assertTrue(callResult.successful);
        Assert.assertEquals(callResult.result,2);
        Assert.assertEquals(callResult.retries,1);
        Assert.assertNotNull(callResult.exception);
    }
    @Test(groups = { "ClusterUtil" })
    public void callFailTest() {
        int[] t = {0};
        ClusterUtil.CallResult callResult = ClusterUtil.call(3,100,()->{
            t[0]++;
            if(t[0]<4) throw new IllegalArgumentException("less 2");
            return t[0];
        });
        Assert.assertFalse(callResult.successful);
        Assert.assertNull(callResult.result);
        Assert.assertEquals(callResult.retries,3);
        Assert.assertNotNull(callResult.exception);
    }
}
