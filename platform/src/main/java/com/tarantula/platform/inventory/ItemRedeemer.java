package com.tarantula.platform.inventory;


import com.icodesoftware.Configurable;
import com.icodesoftware.service.ApplicationPreSetup;

public class ItemRedeemer extends ApplicationRedeemer {

    public ItemRedeemer(String systemId, PlatformInventoryServiceProvider inventoryServiceProvider){
        super(systemId,inventoryServiceProvider);
    }
    public ItemRedeemer(String systemId, ApplicationPreSetup applicationPreSetup){
        super(systemId,applicationPreSetup);
    }
    public ItemRedeemer(String systemId, ApplicationRedeemer inventoryRedeemer){
        super(systemId,inventoryRedeemer);
    }

    public void redeem() {
        //if(!this.configurationType.equals(Configurable.ITEM_CONFIG_TYPE)) return;
        reference.forEach((ref)->{
            ItemRedeemer inventoryRedeemer = new ItemRedeemer(systemId,this.applicationPreSetup);
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
}
