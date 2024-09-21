package com.tarantula.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.icodesoftware.DataStore;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.platform.item.ConfigurableEdit;
import com.tarantula.platform.item.PropertyEdit;
import com.tarantula.platform.item.PropertyEditQuery;
import com.tarantula.platform.tournament.RangedTournamentPrize;
import com.tarantula.platform.tournament.TournamentPrize;
import com.tarantula.platform.tournament.TournamentSchedule;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.List;

public class ConfigurableEditTest extends DataStoreHook{



    @Test(groups = { "ConfigurableEdit" })
    public void setupTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_configurable_edit");
        ConfigurableEdit configurableEdit = new ConfigurableEdit();
        configurableEdit.configurationId = 1;
        configurableEdit.configurationName("name");
        configurableEdit.configurationType("type");
        configurableEdit.configurationTypeId("typeId");
        configurableEdit.configurationVersion("version");
        configurableEdit.configurationCategory("category");
        configurableEdit.configurationScope("scope");
        Assert.assertTrue(dataStore.create(configurableEdit));

        ConfigurableEdit load = new ConfigurableEdit();
        load.distributionId(configurableEdit.distributionId());
        Assert.assertTrue(dataStore.load(load));
        Assert.assertEquals(load.configurationId,configurableEdit.configurationId);
        Assert.assertEquals(load.configurationName(),configurableEdit.configurationName());
        Assert.assertEquals(load.configurationType(),configurableEdit.configurationType());
        Assert.assertEquals(load.configurationTypeId(),configurableEdit.configurationTypeId());
        Assert.assertEquals(load.configurationScope(),configurableEdit.configurationScope());
        Assert.assertEquals(load.configurationCategory(),configurableEdit.configurationCategory());
        Assert.assertEquals(load.configurationVersion(),configurableEdit.configurationVersion());

        PropertyEdit prop = new PropertyEdit();
        prop.name("Level");
        prop.type ="number";
        prop.edit = new JsonPrimitive(1);
        prop.ownerKey(configurableEdit.key());
        Assert.assertTrue(dataStore.create(prop));

        List<PropertyEdit> edits = dataStore.list(new PropertyEditQuery(configurableEdit.key()));
        Assert.assertEquals(edits.size(),1);
        Assert.assertEquals(edits.get(0).edit.getAsInt(),1);
        Assert.assertEquals(edits.get(0).type,"number");
        Assert.assertEquals(edits.get(0).name(),"Level");

        PropertyEdit prop1 = new PropertyEdit();
        prop1.name("Sku");
        prop1.type = "category";
        JsonArray arr = new JsonArray();
        arr.add(1);
        prop1.edit = arr;
        prop1.ownerKey(configurableEdit.key());
        Assert.assertTrue(dataStore.create(prop1));

        List<PropertyEdit> loads = dataStore.list(new PropertyEditQuery(configurableEdit.key()));
        Assert.assertEquals(loads.size(),2);

        ConfigurableEdit edit = new ConfigurableEdit();
        edit.distributionId(configurableEdit.distributionId());
        edit.dataStore(dataStore);
        Assert.assertTrue(dataStore.load(edit));
        //System.out.println(edit.assembly());
    }
    @Test(groups = { "ConfigurableEdit" })
    public void testShop() throws Exception{
        DataStore dataStore = dataStoreProvider.createDataStore("test_configurable_edit_sample");
        try(InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-shop.json")){
            JsonObject payload = JsonUtil.parse(inputStream);
            ConfigurableEdit edit = new ConfigurableEdit();
            edit.dataStore(dataStore);
            edit.build(payload);
            Assert.assertTrue(edit.distributionId()!=0);
            ConfigurableEdit load = new ConfigurableEdit();
            load.distributionId(edit.distributionId());
            load.dataStore(dataStore);
            JsonObject resp = load.assembly();
            //System.out.println(resp);
            Assert.assertNotNull(resp.get("_shoppingItemList"));
        }
    }

    @Test(groups = { "ConfigurableEdit" })
    public void testTournamentSchedule() throws Exception{
        DataStore dataStore = dataStoreProvider.createDataStore("test_configurable_edit_sample");
        try(InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-tournament-schedule.json")){
            JsonObject payload = JsonUtil.parse(inputStream);
            ConfigurableEdit edit = new ConfigurableEdit();
            edit.dataStore(dataStore);
            edit.build(payload);
            //System.out.println(edit.distributionId());
            Assert.assertTrue(edit.distributionId()!=0);
            ConfigurableEdit load = new ConfigurableEdit();
            load.distributionId(edit.distributionId());
            load.dataStore(dataStore);
            JsonObject resp = load.assembly();
            //System.out.println(resp);
            Assert.assertNotNull(resp.get("_prizeSet"));

        }
    }

}
