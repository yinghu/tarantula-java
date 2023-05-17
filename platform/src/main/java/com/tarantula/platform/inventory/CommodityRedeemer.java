package com.tarantula.platform.inventory;


import com.icodesoftware.DataStore;

public class CommodityRedeemer extends ApplicationRedeemer {

    public CommodityRedeemer(String systemId, ApplicationRedeemer inventoryRedeemer){
        super(systemId,inventoryRedeemer);
    }

    public void redeem() {
        int cindex = this.configurationCategory.indexOf(".");
        Inventory inventory = this.inventoryServiceProvider.newInventory(cindex<0?this.configurationCategory:this.configurationCategory.substring(0,cindex),this.configurationTypeId);
        inventory.distributionKey(systemId);
        DataStore inventoryDataStore = this.inventoryServiceProvider.inventoryDataStore();
        inventoryDataStore.createIfAbsent(inventory,true);
        inventory.dataStore(inventoryDataStore);
        inventory.redeem(this);
    }
}
