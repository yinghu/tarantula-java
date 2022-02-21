package com.tarantula.platform.inventory;


public class CommodityRedeemer extends ApplicationRedeemer {

    public CommodityRedeemer(String systemId, ApplicationRedeemer inventoryRedeemer){
        super(systemId,inventoryRedeemer);
    }

    public void redeem() {
        Inventory inventory = this.inventoryServiceProvider.newInventory(this.configurationCategory);
        inventory.distributionKey(systemId);
        dataStore.createIfAbsent(inventory,true);
        inventory.dataStore(dataStore);
        inventory.redeem(this);
    }
}
