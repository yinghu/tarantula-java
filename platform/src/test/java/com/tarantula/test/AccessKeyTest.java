package com.tarantula.test;

import com.icodesoftware.DataStore;

import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.service.AccessKey;
import com.tarantula.platform.service.AccessKeyQuery;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.List;

public class AccessKeyTest extends DataStoreHook{

    @Test(groups = { "DataStore" })
    public void smokeTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("access_key_test");
        SnowflakeKey ownerId = new SnowflakeKey(serviceContext.distributionId());
        AccessKey accessKey = new AccessKey();
        accessKey.typeId("test");
        accessKey.index(""+this.serviceContext.node().bucketId());
        accessKey.timestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        accessKey.disabled(false);
        accessKey.ownerKey(ownerId);
        Assert.assertTrue(dataStore.create(accessKey));

        AccessKey load = new AccessKey();
        load.distributionId(accessKey.distributionId());
        Assert.assertTrue(dataStore.load(load));
        Assert.assertTrue(load.typeId().equals(accessKey.typeId()));

        AccessKey loadIf = new AccessKey();
        loadIf.distributionId(load.distributionId());
        Assert.assertFalse(dataStore.createIfAbsent(loadIf,true));
        Assert.assertTrue(loadIf.typeId().equals(load.typeId()));

        AccessKeyQuery query = new AccessKeyQuery(ownerId.snowflakeId());
        List<AccessKey> keys = dataStore.list(query);
        Assert.assertTrue(keys.size()==1);
        Assert.assertTrue(accessKey.typeId().equals(keys.get(0).typeId()));

        AccessKey updating = keys.get(0);
        updating.disabled(true);
        Assert.assertTrue(dataStore.update(updating));
        AccessKey updated = new AccessKey();
        updated.distributionId(updating.distributionId());

        Assert.assertTrue(dataStore.load(updated));
        Assert.assertTrue(updated.disabled());
        Assert.assertTrue(updated.revision()==updating.revision());

    }


}
