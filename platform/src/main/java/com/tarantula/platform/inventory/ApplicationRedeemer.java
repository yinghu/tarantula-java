package com.tarantula.platform.inventory;

import com.icodesoftware.Configurable;
import com.tarantula.platform.item.ConfigurableObject;


public class ApplicationRedeemer extends ConfigurableObject{

    protected String systemId;
    protected PlatformInventoryServiceProvider inventoryServiceProvider;

    public ApplicationRedeemer(String systemId, PlatformInventoryServiceProvider inventoryServiceProvider){
        this.systemId = systemId;
        this.inventoryServiceProvider = inventoryServiceProvider;
    }
    public ApplicationRedeemer(String systemId, ApplicationRedeemer inventoryRedeemer){
        this.systemId = systemId;
        this.configurationType = inventoryRedeemer.configurationType;
        this.configurationTypeId = inventoryRedeemer.configurationTypeId;
        this.configurationName = inventoryRedeemer.configurationName;
        this.configurationCategory = inventoryRedeemer.configurationCategory;
        this.configurationVersion = inventoryRedeemer.configurationVersion;
        this.header = inventoryRedeemer.header;
        this.application = inventoryRedeemer.application;
        this.reference = inventoryRedeemer.reference;
        this.inventoryServiceProvider = inventoryRedeemer.inventoryServiceProvider;
        this.distributionKey(inventoryRedeemer.distributionKey());
    }


    public  void redeem(){
        //if(!this.configurationType.equals(Configurable.ITEM_CONFIG_TYPE)) return;
        reference.forEach((ref)->{
            ApplicationRedeemer inventoryRedeemer = new ApplicationRedeemer(systemId,this.inventoryServiceProvider);
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
                    ItemRedeemer itemRedeemer = new ItemRedeemer(systemId,inventoryRedeemer);
                    itemRedeemer.dataStore(dataStore);
                    itemRedeemer.redeem();
                }
            }
        });
    }

    public double amount() {
        if(header.has("Amount")) return header.get("Amount").getAsDouble();
        if(header.has("amount")) return header.get("amount").getAsDouble();
        return 0;
    }
}
