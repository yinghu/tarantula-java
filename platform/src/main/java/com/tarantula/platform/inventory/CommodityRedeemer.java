package com.tarantula.platform.inventory;


public class CommodityRedeemer extends InventoryRedeemer{

    public CommodityRedeemer(String systemId,InventoryRedeemer inventoryRedeemer){
        super(systemId,inventoryRedeemer);
    }

    public void redeem() {
        this.inventoryServiceProvider.rechargeable(this.configurationCategory);
        Inventory inventory = new Inventory(this.configurationCategory);
        inventory.distributionKey(systemId);
        dataStore.createIfAbsent(inventory,true);
        inventory.dataStore(dataStore);
        inventory.redeem(this);
    }
}
