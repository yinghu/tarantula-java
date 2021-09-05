package com.tarantula.platform.inventory;

import com.icodesoftware.Configurable;
import com.tarantula.platform.item.Asset;
import com.tarantula.platform.item.Commodity;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.Item;

import java.util.ArrayList;

public class InventoryRedeemer extends ConfigurableObject {

    private String systemId;
    private ArrayList<InventoryRedeemer> _pendingCommodity;
    public InventoryRedeemer(String systemId){
        this.systemId = systemId;
        _pendingCommodity = new ArrayList<>();
    }

    @Override
    public  <T extends Configurable> T setup(){
        if(this.configurationType.equals(Configurable.ASSET_CONFIG_TYPE)){
            //Asset asset = new Asset(this);
            //asset.dataStore(dataStore);
            //return asset.setup();
        }
        if(this.configurationType.equals(Configurable.COMMODITY_CONFIG_TYPE)){
            //Commodity commodity = new Commodity(this);
            //commodity.dataStore(dataStore);
            //return commodity.setup();
        }
        if(this.configurationType.equals(Configurable.ITEM_CONFIG_TYPE)){
            //Item item = new Item(this);
            //item.dataStore(dataStore);
            //return item.setup();
        }
        return null;
    }
}
