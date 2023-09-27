package com.tarantula.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;

import com.icodesoftware.Transaction;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.admin.GameClusterQuery;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.*;
import com.tarantula.platform.service.ApplicationPreSetup;
import org.testng.Assert;
import org.testng.annotations.Test;


public class GameClusterTest extends DataStoreHook{


    @Test(groups = { "GameCluster" })
    public void createGameCluster(){
        DataStore ds = dataStoreProvider.createDataStore("test_tarantula");
        long accountId = serviceContext.distributionId();
        long publishingId = serviceContext.distributionId();
        GameCluster gameCluster = new GameCluster();
        gameCluster.distributionId(publishingId);
        gameCluster.name("beam");
        gameCluster.mode = "pve";
        gameCluster.developerIcon = "dicon";
        gameCluster.gameIcon = "gaicon";
        gameCluster.developer = "gdds";
        gameCluster.tournamentEnabled = true;
        gameCluster.dedicated = false;
        gameCluster.applicationSetup = "com.tarantula.game.service.GameConfigurationSetup";
        gameCluster.maxArenaCount = 10;
        gameCluster.maxZoneCount = 10;
        gameCluster.maxLobbyCount = 10;
        gameCluster.maxDataSize = 4000;
        gameCluster.upgradeVersion = 1;
        Assert.assertTrue(ds.createIfAbsent(gameCluster,false));
        Assert.assertNull(gameCluster.gameLobbyName);
        gameCluster.gameLobbyName = "beam-lobby";
        gameCluster.gameServiceName = "beam-service";
        Assert.assertTrue(ds.update(gameCluster));

        GameCluster load = new GameCluster();
        load.distributionId(gameCluster.distributionId());
        Assert.assertTrue(ds.load(load));
        Assert.assertEquals(load.name(),gameCluster.name());
        Assert.assertNotNull(load.gameLobbyName);

        gameCluster.ownerKey(new SnowflakeKey(accountId));
        gameCluster.onEdge(true);
        Assert.assertTrue(ds.createEdge(gameCluster,"gameCluster"));
        gameCluster.ownerKey(new SnowflakeKey(publishingId));
        Assert.assertTrue(ds.createEdge(gameCluster,"gameCluster"));
        Assert.assertEquals(ds.list(new GameClusterQuery(accountId)).size(),1);
        Assert.assertEquals(ds.list(new GameClusterQuery(publishingId)).size(),1);
        load.setup(serviceContext);

        ConfigurableTemplate configuration = serviceContext.deploymentServiceProvider().configuration(load,"sample-game-asset-category-settings");
        JsonArray items = (JsonArray) configuration.property("itemList");
        Assert.assertNotNull(items);
        ConfigurableCategories categories = new ConfigurableCategories();
        ConfigurableTypes types = new ConfigurableTypes();
        types.name(ConfigurableObject.ASSET_CONFIG_TYPE);
        categories.name(ConfigurableObject.ASSET_CONFIG_TYPE);


        Transaction transaction = load.transaction();
        transaction.execute((ctx)->{
            ApplicationPreSetup preSetup = (ApplicationPreSetup)ctx;
            items.forEach(item->{
                JsonObject jo = item.getAsJsonObject();
                ConfigurableCategory category = new ConfigurableCategory(jo);
                category.onEdge(true);
                category.label("category");
                category.ownerKey(ConfigurableCategoryQuery.AssetKey);
                Assert.assertNotNull(category.name());
                Assert.assertTrue(preSetup.save(load,category));
                //category.ownerKey(ConfigurableCategoryQuery.ComponentKey);
                //Assert.assertTrue(preSetup.edge(load,category,"category"));
                //ConfigurableType type = category.configurableType();
                //type.ownerKey(ConfigurableTypeQuery.AssetKey);
                //type.onEdge(true);
                //type.label("type");
                //preSetup.save(load,type);
                //type.ownerKey(ConfigurableTypeQuery.CommodityKey);
                //Assert.assertTrue(preSetup.edge(load,type,"type"));
                //Assert.assertTrue(categories.addCategory(category));
                //Assert.assertTrue(types.addType(type));
                //ConfigurableCategory load = new ConfigurableCategory();
                //load.name(category.name());
                //Assert.assertTrue(gameObjectSetup.load(app,category));
                //System.out.println(load.configurableType().toJson().toString());
            });
            return true;
        });
    }

}
