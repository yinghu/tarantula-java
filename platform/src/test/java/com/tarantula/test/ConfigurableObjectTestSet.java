package com.tarantula.test;


import com.icodesoftware.DataStore;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;

import com.mysql.cj.xdevapi.JsonArray;
import com.tarantula.game.service.GameObjectSetup;
import com.tarantula.platform.item.ConfigurableTemplate;
import com.tarantula.platform.item.JsonConfigurableTemplateParser;

import com.tarantula.platform.service.ApplicationPreSetup;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;



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

        ConfigurableTemplate template = JsonConfigurableTemplateParser.itemSet(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-game-asset-category-settings.json"));
        JsonArray items = (JsonArray) template.property("itemList");
        Assert.assertNotNull(items);

    }

}
