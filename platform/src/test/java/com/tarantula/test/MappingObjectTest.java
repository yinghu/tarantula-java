package com.tarantula.test;


import com.icodesoftware.DataStore;

import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.platform.presence.MappingObject;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.util.RecoverableQuery;
import org.testng.Assert;
import org.testng.annotations.Test;


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
}
