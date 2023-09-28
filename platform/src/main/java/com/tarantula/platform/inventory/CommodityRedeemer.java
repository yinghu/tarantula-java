package com.tarantula.platform.inventory;


import com.icodesoftware.DataStore;
import com.icodesoftware.util.SnowflakeKey;

public class CommodityRedeemer extends ApplicationRedeemer {

    public CommodityRedeemer(String systemId, ApplicationRedeemer inventoryRedeemer){
        super(systemId,inventoryRedeemer);
    }

    public void redeem() {
        int cindex = this.configurationCategory.indexOf(".");
        Inventory inventory = this.inventoryServiceProvider.newInventory(cindex<0?this.configurationCategory:this.configurationCategory.substring(0,cindex),this.configurationTypeId);
        inventory.ownerKey(new SnowflakeKey(Long.parseLong(systemId)));
        DataStore inventoryDataStore = this.inventoryServiceProvider.inventoryDataStore();
        inventoryDataStore.create(inventory);
        inventory.dataStore(inventoryDataStore);
        inventory.redeem(this,this.inventoryServiceProvider);
    }
}
