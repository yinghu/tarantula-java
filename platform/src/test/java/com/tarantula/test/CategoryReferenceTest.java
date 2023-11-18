package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.util.NaturalKey;
import com.tarantula.platform.item.CategoryReference;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CategoryReferenceTest extends DataStoreHook{


    @Test(groups = { "CategoryReference" })
    public void categoryReferenceTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_config_ref");
        CategoryReference instanceReference1 = new CategoryReference("String","GameView");
        CategoryReference instanceReference2 = new CategoryReference("String","Sku");
        CategoryReference instanceReference3 = new CategoryReference("String","Mill");
        Assert.assertTrue(dataStore.createEdge(instanceReference1,"category"));
        Assert.assertTrue(dataStore.createEdge(instanceReference2,"category"));
        Assert.assertFalse(dataStore.createEdge(instanceReference2,"category"));
        Assert.assertFalse(dataStore.createEdge(instanceReference2,"category"));
        Assert.assertTrue(dataStore.createEdge(instanceReference3,"category"));
        int[] ct = {0};
        dataStore.backup().forEachEdgeKey(new NaturalKey("String"),"category",(k,v)->{
            ct[0]++;
            //System.out.println(v.readUTF8());
            return true;
        });
        Assert.assertEquals(ct[0],3);
    }

}
