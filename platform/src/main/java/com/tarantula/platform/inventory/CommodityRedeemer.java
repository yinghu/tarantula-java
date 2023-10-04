package com.tarantula.platform.inventory;


import com.icodesoftware.DataStore;
import com.icodesoftware.util.SnowflakeKey;

public class CommodityRedeemer extends ApplicationRedeemer {

    public CommodityRedeemer(String systemId, ApplicationRedeemer inventoryRedeemer){
        super(systemId,inventoryRedeemer);
    }

    public void redeem() {
        int cindex = this.configurationCategory.indexOf(".");
        String type = cindex<0?this.configurationCategory:this.configurationCategory.substring(0,cindex);
        InventoryQuery query = new InventoryQuery(Long.parseLong(systemId));
        DataStore inventoryDataStore = this.inventoryServiceProvider.inventoryDataStore();
        UserInventory[] inventories = {null};
        inventoryDataStore.list(query,t->{
            if(t.type.equals(type)&&t.typeId.equals(this.configurationTypeId)){
                inventories[0]=t;
                return false;
            }
            return true;
        });
        if(inventories[0]!=null){
            inventories[0].dataStore(inventoryDataStore);
            inventories[0].redeem(this,this.inventoryServiceProvider);
            return;
        }
        UserInventory inventory = this.inventoryServiceProvider.newInventory(type,this.configurationTypeId);
        inventory.ownerKey(new SnowflakeKey(Long.parseLong(systemId)));
        inventoryDataStore.create(inventory);
        inventoryDataStore.createEdge(inventory,this.configurationTypeId);
        inventoryDataStore.createEdge(inventory,type);
        inventory.dataStore(inventoryDataStore);
        inventory.redeem(this,this.inventoryServiceProvider);
    }
}
