package com.tarantula.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.icodesoftware.DataStore;
import com.tarantula.platform.item.ConfigurableEdit;
import com.tarantula.platform.item.PropertyEdit;
import com.tarantula.platform.item.PropertyEditQuery;
import org.testng.Assert;
import org.testng.annotations.Test;

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
        Assert.assertTrue(dataStore.createIfAbsent(configurableEdit,false));

        ConfigurableEdit load = new ConfigurableEdit();
        load.configurationId = 1;
        Assert.assertTrue(dataStore.load(load));
        Assert.assertEquals(load.configurationId,configurableEdit.configurationId);
        Assert.assertEquals(load.configurationName(),configurableEdit.configurationName());
        Assert.assertEquals(load.configurationType(),configurableEdit.configurationType());
        Assert.assertEquals(load.configurationTypeId(),configurableEdit.configurationTypeId());
        Assert.assertEquals(load.configurationScope(),configurableEdit.configurationScope());
        Assert.assertEquals(load.configurationCategory(),configurableEdit.configurationCategory());
        Assert.assertEquals(load.configurationVersion(),configurableEdit.configurationVersion());

        PropertyEdit prop = new PropertyEdit();
        prop.edit = new JsonPrimitive(1);
        prop.ownerKey(configurableEdit.key());
        Assert.assertTrue(dataStore.create(prop));

        List<PropertyEdit> edits = dataStore.list(new PropertyEditQuery(configurableEdit.key()));
        Assert.assertEquals(edits.size(),1);
        Assert.assertEquals(edits.get(0).edit.getAsInt(),1);
    }


}
