package com.tarantula.platform.inventory;


import com.icodesoftware.DataStore;

public class CommodityRedeemer extends ApplicationRedeemer {

    public CommodityRedeemer(String systemId, ApplicationRedeemer inventoryRedeemer){
        super(systemId,inventoryRedeemer);
    }

    public void redeem() {
        Inventory inventory = this.inventoryServiceProvider.newInventory(this.configurationCategory,this.configurationTypeId);
        inventory.distributionKey(systemId);
        DataStore inventoryDataStore = this.inventoryServiceProvider.inventoryDataStore();
        inventoryDataStore.createIfAbsent(inventory,true);
        inventory.dataStore(inventoryDataStore);
        System.out.println(inventory.key().asString());
        inventory.redeem(this);
    }
}
