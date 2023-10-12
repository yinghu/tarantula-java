package com.tarantula.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.DataStore;

import com.icodesoftware.Transaction;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.admin.GameClusterQuery;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.List;


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
        //gameCluster.applicationSetup = "com.tarantula.game.service.GameConfigurationSetup";
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
                category.ownerKey(ConfigurableCategoryQuery.ComponentKey);
                Assert.assertTrue(preSetup.edge(load,category,"category"));
                ConfigurableType type = category.configurableType();
                type.ownerKey(ConfigurableTypeQuery.AssetKey);
                type.onEdge(true);
                type.label("type");
                preSetup.save(load,type);
                type.ownerKey(ConfigurableTypeQuery.CommodityKey);
                Assert.assertTrue(preSetup.edge(load,type,"type"));
                Assert.assertTrue(categories.addCategory(category));
                Assert.assertTrue(types.addType(type));
                ConfigurableCategory aload = new ConfigurableCategory();
                aload.name(category.name());
                Assert.assertTrue(preSetup.load(gameCluster,aload));
            });
            return true;
        });
        transaction.close();
        //load.applicationSetup = "com.tarantula.game.service.GameObjectSetup";
        ApplicationPreSetup ex = load.applicationPreSetup();
        Assert.assertEquals(ex.list(load,new ConfigurableCategoryQuery(ConfigurableCategoryQuery.AssetKey,"category")).size(),2);

        DeploymentDescriptor app = new DeploymentDescriptor();
        app.typeId("beam-data");
        app.distributionId(serviceContext.distributionId());
        //load.applicationSetup = "com.tarantula.game.service.GameConfigurationSetup";
        InputStream in = (Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-admin-role-commodity-settings.json"));
        JsonArray commodities = JsonUtil.parse(in).get("list").getAsJsonArray();
        Assert.assertNotNull(commodities);
        Transaction t1 = load.transaction();
        t1.execute(ctx->{
            ApplicationPreSetup preSetup = (ApplicationPreSetup)ctx;
            commodities.forEach(item->{
                JsonObject jo = item.getAsJsonObject();
                Commodity configurableObject = new Commodity();
                Assert.assertTrue(configurableObject.configureAndValidate(jo));
                Assert.assertTrue(preSetup.save(app,configurableObject));
            });
            return true;
        });
        t1.close();
        Assert.assertEquals(ex.list(app,new ConfigurableObjectQuery(app.key(),"commodity")).size(),30);
        Assert.assertEquals(ex.list(app,new ConfigurableObjectQuery(app.key(),"Gem")).size(),6);
        //name
        List<ConfigurableObject> g500 = ex.list(app,new ConfigurableObjectQuery(app.key(),"G500"));
        Assert.assertEquals(g500.size(),1);
        //category
        Assert.assertEquals(ex.list(app,new ConfigurableObjectQuery(app.key(),"HardCurrency")).size(),6);
        //version from g500
        Assert.assertEquals(ex.list(app,new VersionedConfigurableObjectQuery(g500.get(0).distributionId())).size(),1);

        ConfigurableObject g = g500.get(0);
        g.configurationVersion("v2.0");
        //after update
        Transaction t2 = load.transaction();
        t2.execute(ctx->{
            ApplicationPreSetup preSetup = (ApplicationPreSetup)ctx;
            return preSetup.save(app,g);
        });
        t2.close();
        Assert.assertEquals(ex.list(app,new VersionedConfigurableObjectQuery(g.distributionId())).size(),2);
        //System.out.println(g.application());
        Transaction t3 = load.transaction();
        t3.execute(ctx->{
            ApplicationPreSetup preSetup = (ApplicationPreSetup)ctx;
            return preSetup.delete(app,g);
        });
        t3.close();
        //after delete
        Assert.assertEquals(ex.list(app,new ConfigurableCategoryQuery(app.key(),"G500")).size(),0);
        Assert.assertEquals(ex.list(app,new ConfigurableObjectQuery(app.key(),"commodity")).size(),29);
        Assert.assertEquals(ex.list(app,new ConfigurableObjectQuery(app.key(),"Gem")).size(),5);
        Assert.assertEquals(ex.list(app,new ConfigurableObjectQuery(app.key(),"HardCurrency")).size(),5);
        Assert.assertEquals(ex.list(app,new VersionedConfigurableObjectQuery(g.distributionId())).size(),0);

        DataStore log = dataStoreProvider.createLogDataStore("log_beam_service");
        Assert.assertEquals(log.list(new ConfigurableObjectQuery(app.key(),"commodity")).size(),30);
    }

}
