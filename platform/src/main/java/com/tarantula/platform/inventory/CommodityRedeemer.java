package com.tarantula.platform.inventory;


import com.icodesoftware.DataStore;
import com.icodesoftware.Inventory;
import com.icodesoftware.util.SnowflakeKey;

public class CommodityRedeemer extends ApplicationRedeemer {

    public CommodityRedeemer(long systemId, ApplicationRedeemer inventoryRedeemer){
        super(systemId,inventoryRedeemer);
    }

    public void redeem() {
        //System.out.println("CATEGORY : "+this.configurationCategory);
        //int cindex = this.configurationCategory.indexOf(".");
        //String type = cindex<0?this.configurationCategory:this.configurationCategory.substring(0,cindex);
        UserInventory inventory = (UserInventory)applicationPreSetup.inventory(systemId,this.configurationTypeId);
        DataStore inventoryDataStore = this.applicationPreSetup.onDataStore(Inventory.DataStore);
        if(inventory!=null){
            inventory.applicationPreSetup(applicationPreSetup);
            inventory.redeem(this);
            return;
        }
        inventory = (UserInventory)this.applicationPreSetup.createInventory(this.configurationCategory,this.configurationTypeId);
        inventory.ownerKey(new SnowflakeKey(systemId));
        inventoryDataStore.create(inventory);
        inventoryDataStore.createEdge(inventory,this.configurationTypeId);
        inventoryDataStore.createEdge(inventory,this.configurationCategory);
        inventory.dataStore(inventoryDataStore);
        inventory.applicationPreSetup(applicationPreSetup);
        inventory.redeem(this);
    }
}
