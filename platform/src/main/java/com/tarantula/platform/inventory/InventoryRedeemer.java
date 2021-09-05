package com.tarantula.platform.inventory;

import com.icodesoftware.Configurable;
import com.tarantula.platform.item.Commodity;
import com.tarantula.platform.item.ConfigurableObject;


public class InventoryRedeemer extends ConfigurableObject {

    private String systemId;

    public InventoryRedeemer(String systemId){
        this.systemId = systemId;
    }
    public InventoryRedeemer(String systemId,InventoryRedeemer inventoryRedeemer){
        this.systemId = systemId;
        this.configurationType = inventoryRedeemer.configurationType;
        this.configurationTypeId = inventoryRedeemer.configurationTypeId;
        this.configurationName = inventoryRedeemer.configurationName;
        this.configurationCategory = inventoryRedeemer.configurationCategory;
        this.configurationVersion = inventoryRedeemer.configurationVersion;
        this.header = inventoryRedeemer.header;
        this.application = inventoryRedeemer.application;
        this.payload = inventoryRedeemer.payload;
        this.reference = inventoryRedeemer.reference;
        this.distributionKey(inventoryRedeemer.distributionKey());
    }

    @Override
    public  <T extends Configurable> T setup(){
        if(this.configurationType.equals(Configurable.ASSET_CONFIG_TYPE)){
            //skip
        }
        else if(this.configurationType.equals(Configurable.COMMODITY_CONFIG_TYPE)){
            reference.forEach((ref)->{//redeem commodity
                ConfigurableObject cop = new ConfigurableObject();
                cop.distributionKey(ref.getAsString());
                if(this.dataStore.load(cop)){
                    Inventory inventory = new Inventory(cop.configurationCategory());
                    inventory.distributionKey(systemId);
                    this.dataStore.createIfAbsent(inventory,true);
                    inventory.dataStore(dataStore);
                    inventory.redeem(new Commodity(cop));
                }
            });
        }
        else if(this.configurationType.equals(Configurable.ITEM_CONFIG_TYPE)){
            //redeem item
            InventoryRedeemer item = new InventoryRedeemer(systemId,this);
            item.dataStore(dataStore);
            item.setup();
        }
        return null;
    }
}
