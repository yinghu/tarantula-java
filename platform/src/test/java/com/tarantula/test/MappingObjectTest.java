package com.tarantula.test;


import com.icodesoftware.DataStore;

import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.platform.configuration.ConfigurationObject;
import com.tarantula.platform.item.ItemPortableRegistry;
import com.tarantula.platform.presence.MappingObject;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.util.RecoverableQuery;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;


public class MappingObjectTest extends DataStoreHook{

    @Test(groups = { "MappingObject" })
    public void mappingObjectTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_mo_object");
        long id = serviceContext.distributionId();
        byte[] jsonArray = "[1,2,3]".getBytes();
        MappingObject mappingObject = new MappingObject();
        mappingObject.value(jsonArray);
        mappingObject.label("test");
        mappingObject.name("value1");
        mappingObject.ownerKey(new SnowflakeKey(id));

        MappingObject mappingObject1 = new MappingObject();
        mappingObject1.value(jsonArray);
        mappingObject1.label("test");
        mappingObject1.name("value2");
        mappingObject1.ownerKey(new SnowflakeKey(id));

        Assert.assertTrue(dataStore.create(mappingObject));
        Assert.assertTrue(dataStore.create(mappingObject1));

        RecoverableQuery<MappingObject> query = new RecoverableQuery<>(new SnowflakeKey(id),"test", PresencePortableRegistry.MAPPING_OBJECT_CID,PresencePortableRegistry.INS);
        dataStore.list(query,m->{
            Assert.assertNotNull(m.name());
            Assert.assertEquals(new String(m.value()),"[1,2,3]");return true;
        });
    }

    @Test(groups = { "MappingObject" })
    public void configurationObjectTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_coo_object");
        long id = serviceContext.distributionId();
        byte[] jsonArray = "[1,2,3]".getBytes();
        ConfigurationObject mappingObject = new ConfigurationObject("webhook");
        mappingObject.value(jsonArray);
        mappingObject.ownerKey(new SnowflakeKey(id));

        ConfigurationObject mappingObject1 = new ConfigurationObject("jdbc");
        mappingObject1.value(jsonArray);
        mappingObject1.ownerKey(new SnowflakeKey(id));

        Assert.assertTrue(dataStore.create(mappingObject));
        Assert.assertTrue(dataStore.create(mappingObject1));

        RecoverableQuery<ConfigurationObject> query = new RecoverableQuery<>(new SnowflakeKey(id),ConfigurationObject.LABEL, ItemPortableRegistry.CONFIGURATION_OBJECT_CID,ItemPortableRegistry.INS);
        List<ConfigurationObject> clist = dataStore.list(query);
        Assert.assertEquals(clist.size(),2);
        clist.forEach(m->{
            m.value("[1,2,3,4]".getBytes());
            dataStore.update(m);
        });
        dataStore.list(query,m->{
            Assert.assertEquals(new String(m.value()),"[1,2,3,4]");
            return true;
        });
        clist.forEach(m->{
            dataStore.delete(m);
        });
        Assert.assertEquals(dataStore.list(query).size(),0);
    }

}
