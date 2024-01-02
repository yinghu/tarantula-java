package com.tarantula.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.tarantula.game.service.GameObjectSetup;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class GameObjectSetupDeleteTest extends DataStoreHook{


    @Test(groups = { "GameObjectSetup" })
    public void configDeletedTest() {
        GameCluster gc = new GameCluster();
        gc.gameServiceName = "deleted-game-service";
        gc.gameLobbyName = "deleted-game-lobby";
        gc.gameDataName = "deleted-game-data";
        GameObjectSetup gameObjectSetup = new GameObjectSetup(gc);
        gameObjectSetup.setup(serviceContext);
        DeploymentDescriptor app = new DeploymentDescriptor();
        app.typeId("deleted-game-data");
        app.distributionId(serviceContext.distributionId());
        Configuration configuration = serviceContext.configuration("sample-admin-role-commodity-settings");
        Assert.assertNotNull(configuration);
        JsonArray list = ((JsonElement)configuration.property("list")).getAsJsonArray();
        List<ConfigurableObject> added = new ArrayList<>();
        list.forEach(e->{
            JsonObject jo = e.getAsJsonObject();
            ConfigurableObject co = new ConfigurableObject();
            Assert.assertTrue(co.configureAndValidate(jo));
            Assert.assertTrue(gameObjectSetup.save(app,co));
            co.ownerKey(app.key());
            Assert.assertTrue(gameObjectSetup.edge(app,co,"test"));
            added.add(co);

        });
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"commodity")).size(),30);
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"Gem")).size(),6);
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"test")).size(),30);
        //added.forEach(d->{
        Assert.assertTrue(gameObjectSetup.delete(app,added.get(0)));
        //});
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"commodity")).size(),29);
        //Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"Gem")).size(),0);
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"test")).size(),29);
    }


}
