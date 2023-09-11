package com.tarantula.test;


import com.icodesoftware.DataStore;

import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.game.GamePortableRegistry;
import com.tarantula.game.MappingObject;
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
        mappingObject.ownerKey(new SnowflakeKey(id));
        Assert.assertTrue(dataStore.create(mappingObject));
        RecoverableQuery<MappingObject> query = new RecoverableQuery<>(new SnowflakeKey(id),"mo_test", GamePortableRegistry.MAPPING_OBJECT_CID,GamePortableRegistry.INS);
        dataStore.list(query,m->{
            Assert.assertEquals(new String(m.value()),"[1,2,3]");return true;
        });
    }
}
