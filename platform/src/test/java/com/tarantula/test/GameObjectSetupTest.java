package com.tarantula.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.icodesoftware.Configuration;
import com.icodesoftware.DataStore;

import com.tarantula.game.service.GameObjectSetup;

import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.*;

import org.testng.Assert;

import org.testng.annotations.Test;

public class GameObjectSetupTest extends DataStoreHook{

    @Test(groups = { "GameObjectSetup" })
    public void serviceDataStoreTest() {
        GameObjectSetup gameObjectSetup = new GameObjectSetup();
        gameObjectSetup.setup(serviceContext);
        DeploymentDescriptor app = new DeploymentDescriptor();
        app.typeId("holee-game-data");
        DataStore dataStore1 = gameObjectSetup.dataStore(app);
        Assert.assertEquals(dataStore1.name(),"holee_game_service");

        app.typeId("holee-game-lobby");
        DataStore dataStore2 = gameObjectSetup.dataStore(app);
        Assert.assertEquals(dataStore2.name(),"holee_game_service");

        app.typeId("holee-game-service");
        DataStore dataStore3 = gameObjectSetup.dataStore(app);
        Assert.assertEquals(dataStore3.name(),"holee_game_service");

    }
    @Test(groups = { "GameObjectSetup" })
    public void configDataStoreTest() {
        GameObjectSetup gameObjectSetup = new GameObjectSetup();
        gameObjectSetup.setup(serviceContext);
        GameCluster app = new GameCluster();
        app.gameServiceName = "holee-game-service";
        app.gameLobbyName = "holee-game-lobby";
        app.gameDataName = "holee-game-data";
        DataStore dataStore1 = gameObjectSetup.dataStore(app);
        Assert.assertEquals(dataStore1.name(),"holee_game_service");

        DataStore dataStore2 = gameObjectSetup.dataStore(app);
        Assert.assertEquals(dataStore2.name(),"holee_game_service");

        DataStore dataStore3 = gameObjectSetup.dataStore(app);
        Assert.assertEquals(dataStore3.name(),"holee_game_service");

        DataStore dataStore4 = gameObjectSetup.dataStore(app,"foo");
        Assert.assertEquals(dataStore4.name(),"holee_game_service_foo");

    }
    @Test(groups = { "GameObjectSetup" })
    public void configSettingTest() {
        GameObjectSetup gameObjectSetup = new GameObjectSetup();
        gameObjectSetup.setup(serviceContext);
        GameCluster app = new GameCluster();
        app.gameServiceName = "holee-service";
        app.gameLobbyName = "holee-lobby";
        app.gameDataName = "holee-data";
        ConfigurableTemplate template = JsonConfigurableTemplateParser.itemSet(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-game-asset-category-settings.json"));
        JsonArray items = (JsonArray) template.property("itemList");
        Assert.assertNotNull(items);
        items.forEach(item->{
            JsonObject jo = item.getAsJsonObject();
            ConfigurableCategory category = new ConfigurableCategory(jo);
            category.onEdge(true);
            category.label("category");
            category.ownerKey(ConfigurableCategoryQuery.AssetKey);
            Assert.assertNotNull(category.name());
            Assert.assertTrue(gameObjectSetup.save(app,category));
            category.parse();
            category.ownerKey(ConfigurableCategoryQuery.ComponentKey);
            Assert.assertTrue(gameObjectSetup.edge(app,category,"category"));
            ConfigurableType type = category.configurableType();
            type.ownerKey(ConfigurableTypeQuery.AssetKey);
            type.onEdge(true);
            type.label("type");
            gameObjectSetup.save(app,type);
            type.ownerKey(ConfigurableTypeQuery.CommodityKey);
            Assert.assertTrue(gameObjectSetup.edge(app,type,"type"));
            Assert.assertFalse(gameObjectSetup.edge(app,type,"type"));
            ConfigurableCategory load = new ConfigurableCategory();
            load.name(category.name());
            Assert.assertTrue(gameObjectSetup.load(app,load));
            load.parse();
            Assert.assertEquals(load.name(),category.name());
            Assert.assertEquals(load.scope,category.scope);
            Assert.assertEquals(load.rechargeable,category.rechargeable);
            Assert.assertEquals(load.version,category.version);
            Assert.assertEquals(load.description,category.description);
            Assert.assertEquals(load.application,category.application);
            Assert.assertEquals(load.header,category.header);
        });
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableCategoryQuery(ConfigurableCategoryQuery.AssetKey,"category")).size(),2);
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableCategoryQuery(ConfigurableCategoryQuery.ComponentKey,"category")).size(),2);
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableTypeQuery(ConfigurableTypeQuery.AssetKey,"type")).size(),2);
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableTypeQuery(ConfigurableTypeQuery.CommodityKey,"type")).size(),2);
    }

    @Test(groups = { "GameObjectSetup" })
    public void configCreateObjectTest() {
        GameObjectSetup gameObjectSetup = new GameObjectSetup();
        gameObjectSetup.setup(serviceContext);
        DeploymentDescriptor app = new DeploymentDescriptor();
        app.typeId("holee-game-data");
        //app.oid("gameApp");
        Configuration configuration = serviceContext.configuration("sample-admin-role-commodity-settings");
        Assert.assertNotNull(configuration);
        JsonArray list = ((JsonElement)configuration.property("list")).getAsJsonArray();
        list.forEach(e->{
            JsonObject jo = e.getAsJsonObject();
            ConfigurableObject co = new ConfigurableObject();
            Assert.assertTrue(co.configureAndValidate(jo));
            Assert.assertTrue(gameObjectSetup.save(app,co));
            Assert.assertEquals(gameObjectSetup.list(app,new VersionedConfigurableObjectQuery(co.distributionId())).size(),1);
        });
        gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"commodity")).forEach(c->{
            c.setup();
            Assert.assertNotEquals(c.toJson().toString(),"{}");
        });
        gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"Gem")).forEach(c->{
            c.setup();
            Assert.assertNotEquals(c.toJson().toString(),"{}");
        });
    }
    @Test(groups = { "GameObjectSetup" })
    public void configDeleteObjectTest() {
        GameObjectSetup gameObjectSetup = new GameObjectSetup();
        gameObjectSetup.setup(serviceContext);
        DeploymentDescriptor app = new DeploymentDescriptor();
        app.typeId("sample-game-data");
        //app.oid("gameApp");
        Configuration configuration = serviceContext.configuration("sample-admin-role-commodity-settings");
        Assert.assertNotNull(configuration);
        JsonArray list = ((JsonElement)configuration.property("list")).getAsJsonArray();
        list.forEach(e->{
            JsonObject jo = e.getAsJsonObject();
            ConfigurableObject co = new ConfigurableObject();
            Assert.assertTrue(co.configureAndValidate(jo));
            Assert.assertTrue(gameObjectSetup.save(app,co));
            Assert.assertTrue(gameObjectSetup.delete(app,co));
        });
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"commodity")).size(),0);
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"Gem")).size(),0);
    }

}
