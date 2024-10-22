package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.platform.inventory.InventoryItem;
import com.tarantula.platform.inventory.UserInventory;
import com.tarantula.platform.item.ItemPortableRegistry;
import com.tarantula.platform.presence.saves.SaveRevisionInfo;
import com.tarantula.platform.util.RecoverableQuery;
import org.testng.Assert;
import org.testng.annotations.Test;


public class SaveRevisionInfoTest extends DataStoreHook{


    @Test(groups = { "SaveRevisionInfo" })
    public void revisionTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_inventory");
        long presenceId = serviceContext.distributionId();
        SaveRevisionInfo saveRevisionInfo = new SaveRevisionInfo();
        saveRevisionInfo.distributionId(presenceId);
        saveRevisionInfo.name("inventory");
        saveRevisionInfo.clientRevisionNumber = 10;
        saveRevisionInfo.deviceId = "device1";
        dataStore.createIfAbsent(saveRevisionInfo,true);

        SaveRevisionInfo load = new SaveRevisionInfo();
        load.distributionId(presenceId);
        load.name("inventory");
        dataStore.createIfAbsent(load,true);
        Assert.assertEquals(saveRevisionInfo.clientRevisionNumber,10);
        Assert.assertEquals(saveRevisionInfo.deviceId,"device1");
        Assert.assertEquals(saveRevisionInfo.clientRevisionNumber,load.clientRevisionNumber);
        Assert.assertEquals(saveRevisionInfo.deviceId,load.deviceId);

        load.clientRevisionNumber = load.clientRevisionNumber+1;
        dataStore.update(load);

        SaveRevisionInfo reload = new SaveRevisionInfo();
        reload.distributionId(presenceId);
        reload.name("inventory");
        dataStore.createIfAbsent(reload,true);
        Assert.assertEquals(reload.clientRevisionNumber,11);

        SaveRevisionInfo tournament = new SaveRevisionInfo();
        tournament.distributionId(presenceId);
        tournament.name("tournament");

        dataStore.createIfAbsent(tournament,true);
        Assert.assertEquals(tournament.clientRevisionNumber,0);
    }

}
