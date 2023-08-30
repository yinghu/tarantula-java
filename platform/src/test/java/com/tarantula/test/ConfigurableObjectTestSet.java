package com.tarantula.test;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;

import com.icodesoftware.util.NaturalKey;
import com.tarantula.platform.item.*;

import com.tarantula.platform.service.ApplicationPreSetup;
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
        //ApplicationPreSetup applicationPreSetup = new GameObjectSetup();
        ConfigurableCategories categories = new ConfigurableCategories();
        categories.name("asset");
        ConfigurableTemplate template = JsonConfigurableTemplateParser.itemSet(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-game-asset-category-settings.json"));
        JsonArray items = (JsonArray) template.property("itemList");
        Assert.assertNotNull(items);
        items.forEach(item->{
            JsonObject jo = item.getAsJsonObject();
            ConfigurableCategory category = new ConfigurableCategory(jo);
            category.ownerKey(new NaturalKey("class/asset"));
            Assert.assertTrue(dataStore.create(category));
            Assert.assertTrue(dataStore.createEdge(category,"category"));
            Assert.assertTrue(dataStore.createEdge(category,"link"));
            Assert.assertTrue(dataStore.createEdge(category,"data"));
        });
        Assert.assertEquals(dataStore.list(new ConfigurableCategoryQuery("category")).size(),2);
        Assert.assertEquals(dataStore.list(new ConfigurableCategoryQuery("link")).size(),2);
        Assert.assertEquals(dataStore.list(new ConfigurableCategoryQuery("data")).size(),2);
    }

    @Test(groups = { "configurableTemplate" })
    public void configurableTypeTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_tarantula_config");
        //ApplicationPreSetup applicationPreSetup = new GameObjectSetup();
        ConfigurableCategories categories = new ConfigurableCategories();
        categories.name("asset");
        ConfigurableTemplate template = JsonConfigurableTemplateParser.itemSet(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-common-type-settings.json"));
        JsonArray items = (JsonArray) template.property("itemList");
        Assert.assertNotNull(items);
        items.forEach(item->{
            JsonObject jo = item.getAsJsonObject();
            ConfigurableType type = new ConfigurableType(jo);
            Assert.assertTrue(dataStore.create(type));
            //ConfigurableCategory category = new ConfigurableCategory(jo);
            //category.ownerKey(new NaturalKey("class/asset"));
            //Assert.assertTrue(dataStore.create(category));
            //Assert.assertTrue(dataStore.createEdge(category,"category"));
            //Assert.assertTrue(dataStore.createEdge(category,"link"));
            //Assert.assertTrue(dataStore.createEdge(category,"data"));
        });
        //Assert.assertEquals(dataStore.list(new ConfigurableCategoryQuery("category")).size(),2);
        //Assert.assertEquals(dataStore.list(new ConfigurableCategoryQuery("link")).size(),2);
        //Assert.assertEquals(dataStore.list(new ConfigurableCategoryQuery("data")).size(),2);
    }

}
