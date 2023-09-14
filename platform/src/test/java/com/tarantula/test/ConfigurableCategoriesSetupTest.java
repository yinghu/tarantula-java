package com.tarantula.test;

import com.google.gson.JsonArray;

import com.google.gson.JsonObject;

import com.icodesoftware.DataStore;
import com.tarantula.game.service.GameObjectSetup;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.*;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConfigurableCategoriesSetupTest extends DataStoreHook{


    //@Test(groups = { "ConfigurableCategories" })
    public void configSettingTest() {
        GameObjectSetup gameObjectSetup = new GameObjectSetup();
        gameObjectSetup.setup(serviceContext);
        GameCluster app = new GameCluster();
        app.gameServiceName = "woop-game-service";
        app.gameLobbyName = "woop-game-lobby";
        app.gameDataName = "woop-game-data";
        ConfigurableTemplate configuration = serviceContext.deploymentServiceProvider().configuration(app,"sample-game-asset-category-settings");
        JsonArray items = (JsonArray) configuration.property("itemList");
        Assert.assertNotNull(items);
        ConfigurableCategories categories = new ConfigurableCategories();
        ConfigurableTypes types = new ConfigurableTypes();
        types.name(ConfigurableObject.ASSET_CONFIG_TYPE);
        categories.name(ConfigurableObject.ASSET_CONFIG_TYPE);
        items.forEach(item->{
            JsonObject jo = item.getAsJsonObject();
            ConfigurableCategory category = new ConfigurableCategory(jo);
            category.onEdge(true);
            category.label("category");
            category.ownerKey(ConfigurableCategoryQuery.AssetKey);
            Assert.assertNotNull(category.name());
            Assert.assertTrue(gameObjectSetup.save(app,category));
            category.ownerKey(ConfigurableCategoryQuery.ComponentKey);
            Assert.assertTrue(gameObjectSetup.edge(app,category,"category"));
            ConfigurableType type = category.configurableType();
            type.ownerKey(ConfigurableTypeQuery.AssetKey);
            type.onEdge(true);
            type.label("type");
            gameObjectSetup.save(app,type);
            type.ownerKey(ConfigurableTypeQuery.CommodityKey);
            Assert.assertTrue(gameObjectSetup.edge(app,type,"type"));
            Assert.assertTrue(categories.addCategory(category));
            Assert.assertTrue(types.addType(type));
            //ConfigurableCategory load = new ConfigurableCategory();
            //load.oid(category.oid());
            //Assert.assertTrue(gameObjectSetup.load(app,category));
            //System.out.println(load.configurableType().toJson().toString());
        });
        gameObjectSetup.list(app,new ConfigurableCategoryQuery(ConfigurableCategoryQuery.AssetKey,"category")).forEach(c->{
            Assert.assertFalse(categories.addCategory(c));
        });
        categories.configurableTypes(types);
        //System.out.println(categories.toJson().toString());
        //Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableCategoryQuery(ConfigurableCategoryQuery.ComponentKey,"category")).size(),2);
        gameObjectSetup.list(app,new ConfigurableTypeQuery(ConfigurableTypeQuery.AssetKey,"type")).forEach(t->{
            Assert.assertFalse(types.addType(t));
        });
        ConfigurableCategories load = new ConfigurableCategories();
        load.name(ConfigurableObject.ASSET_CONFIG_TYPE);
        gameObjectSetup.list(app,new ConfigurableCategoryQuery(ConfigurableCategoryQuery.AssetKey,"category")).forEach(c->{
            Assert.assertTrue(load.addCategory(c));
        });
        ConfigurableTypes tload = new ConfigurableTypes();
        tload.name(ConfigurableObject.ASSET_CONFIG_TYPE);
        gameObjectSetup.list(app,new ConfigurableTypeQuery(ConfigurableTypeQuery.AssetKey,"type")).forEach(t->{
            Assert.assertTrue(tload.addType(t));
        });
        load.configurableTypes(tload);
        Assert.assertEquals(categories.toJson().toString(),load.toJson().toString());
        //System.out.println(load.toJson().toString());
        //Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableTypeQuery(ConfigurableTypeQuery.CommodityKey,"type")).size(),2);
    }

    //@Test(groups = { "ConfigurableCategories" })
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
