package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.OidKey;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.service.AccessKey;
import com.tarantula.platform.service.AccessKeyQuery;
import com.icodesoftware.service.DataStoreProvider;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.List;

public class AccessKeyTest {

    DataStoreProvider dataStoreProvider;
    ServiceContext serviceContext;
    @BeforeClass
    public void setUp() {
        DataStoreTestEvn.setUp();
        dataStoreProvider = DataStoreTestEvn.dataStoreProvider;
        serviceContext = DataStoreTestEvn.serviceContext;
    }

    @Test(groups = { "DataStore" })
    public void smokeTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("access_key");
        OidKey ownerId = new OidKey("TESTKEY");
        AccessKey accessKey = new AccessKey();
        accessKey.typeId("test");
        accessKey.index(""+this.serviceContext.node().bucketId());
        accessKey.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        accessKey.disabled(false);
        accessKey.ownerKey(ownerId);
        Assert.assertTrue(dataStore.create(accessKey));

        AccessKey load = new AccessKey();
        load.oid(accessKey.oid());
        Assert.assertTrue(dataStore.load(load));
        Assert.assertTrue(load.typeId().equals(accessKey.typeId()));

        AccessKey loadIf = new AccessKey();
        loadIf.oid(load.oid());
        Assert.assertFalse(dataStore.createIfAbsent(loadIf,true));
        Assert.assertTrue(loadIf.typeId().equals(load.typeId()));

        AccessKeyQuery query = new AccessKeyQuery(ownerId);
        List<AccessKey> keys = dataStore.list(query);
        Assert.assertTrue(keys.size()==1);
        Assert.assertTrue(accessKey.typeId().equals(keys.get(0).typeId()));

        AccessKey updating = keys.get(0);
        updating.disabled(true);
        Assert.assertTrue(dataStore.update(updating));
        AccessKey updated = new AccessKey();
        updated.oid(updating.oid());

        Assert.assertTrue(dataStore.load(updated));
        Assert.assertTrue(updated.disabled());
        Assert.assertTrue(updated.revision()==updating.revision());

    }


}
