package com.tarantula.test;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;

import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;


public class ConfigurableObjectTestSet extends DataStoreHook{


    @Test(groups = { "ConfigurableObject" })
    public void configurableObjectTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_tarantula_config");
        InputStream in = (Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-admin-role-commodity-settings.json"));
        long ref1 = serviceContext.distributionId();
        //long ref2 = serviceContext.distributionId();
        //System.out.println(ref1);
        //System.out.println(ref2);
        JsonArray items = JsonUtil.parse(in).get("list").getAsJsonArray();
        Assert.assertNotNull(items);
        items.forEach(item->{
            JsonObject jo = item.getAsJsonObject();
            jo.get("reference").getAsJsonArray().add(ref1);
            //jo.get("reference").getAsJsonArray().add(ref2);
            ConfigurableObject configurableObject = new ConfigurableObject();
            Assert.assertTrue(configurableObject.configureAndValidate(jo));
            Assert.assertTrue(dataStore.create(configurableObject));

            ConfigurableObject c = new ConfigurableObject();
            c.distributionId(configurableObject.distributionId());
            Assert.assertTrue(dataStore.load(c));
            c.reference().forEach(d->{
                Assert.assertEquals(d.getAsLong(),ref1);
                //System.out.println(d.getAsLong());
            });
            //System.out.println(">>>>>>>>>>>>>>>>>");
            //ConfigurableCategory category = new ConfigurableCategory(jo);
            //category.ownerKey(ConfigurableCategoryQuery.AssetKey);
            //category.label("category");
            //category.onEdge(true);
            //Assert.assertNotNull(category.name());
            //Assert.assertTrue(dataStore.createIfAbsent(category,false));
            //Assert.assertTrue(dataStore.createEdge(category,"category"));
            //Assert.assertTrue(dataStore.createEdge(category,"link"));
            //Assert.assertTrue(dataStore.createEdge(category,"data"));
            //ConfigurableCategory load = new ConfigurableCategory();
            //load.name(category.name());
            //Assert.assertTrue(dataStore.load(load));
            //Assert.assertEquals(load.name(),category.name());
        });
        //Assert.assertEquals(dataStore.list(new ConfigurableCategoryQuery(ConfigurableCategoryQuery.AssetKey,"category")).size(),2);
        //Assert.assertEquals(dataStore.list(new ConfigurableCategoryQuery(ConfigurableCategoryQuery.AssetKey,"link")).size(),2);
        //Assert.assertEquals(dataStore.list(new ConfigurableCategoryQuery(ConfigurableCategoryQuery.AssetKey,"data")).size(),2);
    }



}
