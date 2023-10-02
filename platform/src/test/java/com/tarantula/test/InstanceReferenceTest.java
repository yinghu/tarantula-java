package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.util.NaturalKey;
import com.tarantula.platform.item.InstanceReference;
import org.testng.Assert;
import org.testng.annotations.Test;

public class InstanceReferenceTest extends DataStoreHook{


    @Test(groups = { "InstanceReference" })
    public void instanceReferenceTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_config_ref");
        InstanceReference instanceReference1 = new InstanceReference("String",serviceContext.distributionId());
        InstanceReference instanceReference2 = new InstanceReference("String",serviceContext.distributionId());
        InstanceReference instanceReference3 = new InstanceReference("String",serviceContext.distributionId());
        Assert.assertTrue(dataStore.createEdge(instanceReference1,"instance"));
        Assert.assertTrue(dataStore.createEdge(instanceReference2,"instance"));
        Assert.assertFalse(dataStore.createEdge(instanceReference2,"instance"));
        Assert.assertFalse(dataStore.createEdge(instanceReference2,"instance"));
        Assert.assertTrue(dataStore.createEdge(instanceReference3,"instance"));
        int[] ct = {0};
        dataStore.backup().forEachEdgeKey(new NaturalKey("String"),"instance",(k,v)->{
            ct[0]++;
            return true;
        });
        Assert.assertEquals(ct[0],3);
    }

}
