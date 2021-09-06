package com.tarantula.platform.inventory;

import com.icodesoftware.Configurable;
import com.tarantula.platform.item.Commodity;
import com.tarantula.platform.item.ConfigurableObject;


public class InventoryRedeemer extends ConfigurableObject {

    protected String systemId;

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


    public  void redeem(){
        reference.forEach((ref)->{
            InventoryRedeemer inventoryRedeemer = new InventoryRedeemer(systemId);
            inventoryRedeemer.distributionKey(ref.getAsString());
            if(dataStore.load(inventoryRedeemer)){
                if(inventoryRedeemer.configurationType().equals(Configurable.ASSET_CONFIG_TYPE)){
                    AssetRedeemer assetRedeemer = new AssetRedeemer(systemId,inventoryRedeemer);
                    assetRedeemer.dataStore(dataStore);
                    assetRedeemer.redeem();
                }
                else if(inventoryRedeemer.configurationType().equals(Configurable.COMMODITY_CONFIG_TYPE)){
                    CommodityRedeemer commodityRedeemer = new CommodityRedeemer(systemId,inventoryRedeemer);
                    commodityRedeemer.dataStore(dataStore);
                    commodityRedeemer.redeem();
                }
                else if(inventoryRedeemer.configurationType().equals(Configurable.ITEM_CONFIG_TYPE)){
                    inventoryRedeemer.dataStore(dataStore);
                    inventoryRedeemer.redeem();
                }
            }
        });
    }
}
