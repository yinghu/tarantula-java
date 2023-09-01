package com.tarantula.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.AccessIndex;
import com.icodesoftware.DataStore;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.service.GameObjectSetup;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.ConfigurableCategory;
import com.tarantula.platform.item.ConfigurableCategoryQuery;
import com.tarantula.platform.item.ConfigurableTemplate;
import com.tarantula.platform.item.JsonConfigurableTemplateParser;
import com.tarantula.platform.util.SystemUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GameObjectSetupTest {

    DataStoreProvider dataStoreProvider;
    ServiceContext serviceContext;
    @BeforeClass
    public void setUp() {
        DataStoreTestEvn.setUp();
        dataStoreProvider = DataStoreTestEvn.dataStoreProvider;
        serviceContext = DataStoreTestEvn.serviceContext;
    }


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
    public void configObjectTest() {
        GameObjectSetup gameObjectSetup = new GameObjectSetup();
        gameObjectSetup.setup(serviceContext);
        GameCluster app = new GameCluster();
        app.gameServiceName = "holee-game-service";
        app.gameLobbyName = "holee-game-lobby";
        app.gameDataName = "holee-game-data";
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
            category.ownerKey(ConfigurableCategoryQuery.ComponentKey);
            Assert.assertTrue(gameObjectSetup.edge(app,category,"category"));
            //ConfigurableCategory load = new ConfigurableCategory();
            //load.oid(category.oid());
            //Assert.assertTrue(gameObjectSetup.load(app,category));
            //System.out.println(load.configurableType().toJson().toString());
        });
        gameObjectSetup.list(app,new ConfigurableCategoryQuery(ConfigurableCategoryQuery.AssetKey,"category")).forEach(c->{
            System.out.println(c.name());
        });
        gameObjectSetup.list(app,new ConfigurableCategoryQuery(ConfigurableCategoryQuery.ComponentKey,"category")).forEach(c->{
            System.out.println(c.name());
        });
    }


}
