package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.util.SnowflakeKey;


import com.tarantula.platform.inventory.Inventory;
import com.tarantula.platform.inventory.InventoryItem;
import com.tarantula.platform.item.ItemPortableRegistry;
import com.tarantula.platform.util.RecoverableQuery;
import org.testng.Assert;
import org.testng.annotations.Test;


public class InventoryTest extends DataStoreHook{


    @Test(groups = { "Inventory" })
    public void inventoryTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_inventory");
        long presenceId = serviceContext.distributionId();
        Inventory gold = new Inventory("HardCurrency","Gold",true);
        gold.ownerKey(new SnowflakeKey(presenceId));
        Assert.assertTrue(dataStore.create(gold));
        InventoryItem item = new InventoryItem();
        item.configurationName("G80");
        item.configurationTypeId("Gold");
        item.ownerKey(gold.key());
        Assert.assertTrue(dataStore.create(item));
        Inventory gem = new Inventory("HardCurrency","Gem",true);
        gem.ownerKey(new SnowflakeKey(presenceId));
        Assert.assertTrue(dataStore.create(gem));
        InventoryItem item1 = new InventoryItem();
        item1.configurationName("G20");
        item1.configurationTypeId("Gem");
        item1.ownerKey(gem.key());
        Assert.assertTrue(dataStore.create(item1));
        RecoverableQuery<Inventory> query = RecoverableQuery.query(presenceId,gold, ItemPortableRegistry.INS);
        dataStore.list(query).forEach(c->{
            RecoverableQuery<InventoryItem> iq = RecoverableQuery.query(c.distributionId(),item, ItemPortableRegistry.INS);
            Assert.assertEquals(dataStore.list(iq).size(),1);
        });
    }

}
