package com.tarantula.test;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.item.*;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;


public class ConfigurableObjectTestSet {

    DataStoreProvider dataStoreProvider;
    ServiceContext serviceContext;
    @BeforeClass
    public void setUp() {
        DataStoreTestEvn.setUp();
        dataStoreProvider = DataStoreTestEvn.dataStoreProvider;
        serviceContext = DataStoreTestEvn.serviceContext;
    }


    @AfterTest
    public void tearDown() throws Exception{
        dataStoreProvider.shutdown();
    }
    @Test(groups = { "configurableTemplate" })
    public void configurableTemplateTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_tarantula_config");
        ConfigurableCategories categories = new ConfigurableCategories();
        categories.name("asset");
        ConfigurableTemplate template = JsonConfigurableTemplateParser.itemSet(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-game-asset-category-settings.json"));
        JsonArray items = (JsonArray) template.property("itemList");
        Assert.assertNotNull(items);
        items.forEach(item->{
            JsonObject jo = item.getAsJsonObject();
            ConfigurableCategory category = new ConfigurableCategory(jo);
            category.ownerKey(ConfigurableCategoryQuery.AssetKey);
            category.label("category");
            category.onEdge(true);
            Assert.assertNotNull(category.name());
            Assert.assertTrue(dataStore.createIfAbsent(category,false));
            //Assert.assertTrue(dataStore.createEdge(category,"category"));
            //Assert.assertTrue(dataStore.createEdge(category,"link"));
            //Assert.assertTrue(dataStore.createEdge(category,"data"));
            ConfigurableCategory load = new ConfigurableCategory();
            load.name(category.name());
            Assert.assertTrue(dataStore.load(load));
            Assert.assertEquals(load.name(),category.name());
        });
        Assert.assertEquals(dataStore.list(new ConfigurableCategoryQuery(ConfigurableCategoryQuery.AssetKey,"category")).size(),2);
        //Assert.assertEquals(dataStore.list(new ConfigurableCategoryQuery(ConfigurableCategoryQuery.AssetKey,"link")).size(),2);
        //Assert.assertEquals(dataStore.list(new ConfigurableCategoryQuery(ConfigurableCategoryQuery.AssetKey,"data")).size(),2);
    }

    @Test(groups = { "configurableTemplate" })
    public void configurableTypeTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_tarantula_config");
        ConfigurableCategories categories = new ConfigurableCategories();
        categories.name("asset");
        ConfigurableTemplate template = JsonConfigurableTemplateParser.itemSet(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-common-type-settings.json"));
        JsonArray items = (JsonArray) template.property("itemList");
        Assert.assertNotNull(items);
        items.forEach(item->{
            JsonObject jo = item.getAsJsonObject();
            ConfigurableType type = new ConfigurableType(jo);
            type.label("type");
            type.onEdge(true);
            type.ownerKey(ConfigurableTypeQuery.AssetKey);
            Assert.assertTrue(dataStore.createIfAbsent(type,false));
            //Assert.assertTrue(dataStore.createEdge(type,"type"));
            //Assert.assertTrue(dataStore.createEdge(type,"link"));
            //Assert.assertTrue(dataStore.createEdge(type,"data"));
        });
        Assert.assertEquals(dataStore.list(new ConfigurableTypeQuery(ConfigurableTypeQuery.AssetKey,"type")).size(),17);
        //Assert.assertEquals(dataStore.list(new ConfigurableTypeQuery(ConfigurableTypeQuery.AssetKey,"link")).size(),17);
        //Assert.assertEquals(dataStore.list(new ConfigurableTypeQuery(ConfigurableTypeQuery.AssetKey,"data")).size(),17);
    }

}
