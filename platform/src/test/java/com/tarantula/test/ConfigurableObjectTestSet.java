package com.tarantula.test;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.icodesoftware.util.CompressUtil;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.service.GameObjectSetup;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;


public class ConfigurableObjectTestSet extends DataStoreHook{


    @Test(groups = { "ConfigurableObject" })
    public void configurableObjectOptsTest() {
        GameCluster gc = new GameCluster();
        gc.gameServiceName = "woop-game-service";
        gc.gameLobbyName = "woop-game-lobby";
        gc.gameDataName = "woop-game-data";
        GameObjectSetup gameObjectSetup = new GameObjectSetup(gc);
        gameObjectSetup.setup(serviceContext);
        DeploymentDescriptor app = new DeploymentDescriptor();
        app.typeId("test-game-data");
        app.distributionId(serviceContext.distributionId());
        InputStream in = (Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-admin-role-commodity-settings.json"));
        JsonArray items = JsonUtil.parse(in).get("list").getAsJsonArray();
        Assert.assertNotNull(items);
        //save
        items.forEach(item->{
            JsonObject jo = item.getAsJsonObject();
            Commodity configurableObject = new Commodity();
            Assert.assertTrue(configurableObject.configureAndValidate(jo));
            Assert.assertTrue(gameObjectSetup.save(app,configurableObject));
        });
        //after save
        //type
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"commodity")).size(),30);
        //typeId
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"Gem")).size(),6);
        //name
        List<ConfigurableObject> g500 = gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"G500"));
        Assert.assertEquals(g500.size(),1);
        //category
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"HardCurrency")).size(),6);
        //version from g500
        Assert.assertEquals(gameObjectSetup.list(app,new VersionedConfigurableObjectQuery(g500.get(0).distributionId())).size(),1);

        //update
        g500.get(0).configurationVersion("v2.0");
        //after update
        Assert.assertTrue(gameObjectSetup.save(app,g500.get(0)));
        Assert.assertEquals(gameObjectSetup.list(app,new VersionedConfigurableObjectQuery(g500.get(0).distributionId())).size(),2);

        //delete
        Assert.assertTrue(gameObjectSetup.delete(app,g500.get(0)));
        //after delete
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableCategoryQuery(app.key(),"G500")).size(),0);
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"commodity")).size(),29);
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"Gem")).size(),5);
        Assert.assertEquals(gameObjectSetup.list(app,new ConfigurableObjectQuery(app.key(),"HardCurrency")).size(),5);
        Assert.assertEquals(gameObjectSetup.list(app,new VersionedConfigurableObjectQuery(g500.get(0).distributionId())).size(),0);
    }

    @Test(groups = { "ConfigurableObject" })
    public void sampleTournamentCompress() {
        Exception exception = null;
        try(InputStream in = (Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-game-tournament-category-settings.json"))){
            JsonObject jsonObject = JsonUtil.parse(in);
            jsonObject.get("itemList").getAsJsonArray().forEach(e->{
                JsonObject je = e.getAsJsonObject();
                int srcLength = je.toString().length();
                //System.out.println(srcLength);
                ByteBuffer src = ByteBuffer.wrap(je.toString().getBytes());
                ByteBuffer dest = ByteBuffer.allocate(srcLength);
                CompressUtil.lz4().compress(src,dest);
                dest.flip();
                byte[] comp = new byte[dest.remaining()];
                dest.get(comp);
                //System.out.println(comp.length);
                src.clear();
                dest.rewind();
                CompressUtil.lz4().decompress(dest,src);
                src.flip();
                Assert.assertEquals(src.remaining(),srcLength);
                //make sure no issue from decompress
                JsonUtil.parse(src.array());
            });

        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNull(exception);
    }

    @Test(groups = { "ConfigurableObject" })
    public void sampleTournamentOverflow() {
        Exception exception = null;
        try(InputStream in = (Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-game-tournament-category-settings.json"))){
            JsonObject jsonObject = JsonUtil.parse(in);
            jsonObject.get("itemList").getAsJsonArray().forEach(e->{
                JsonObject je = e.getAsJsonObject();
                ByteBuffer buffer = ByteBuffer.allocate(1000);
                buffer.put(je.toString().getBytes());
            });

        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNotNull(exception);
    }



}
