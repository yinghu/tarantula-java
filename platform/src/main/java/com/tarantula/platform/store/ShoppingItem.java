package com.tarantula.platform.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.item.*;

import java.util.ArrayList;
import java.util.List;


public class ShoppingItem extends Item{


    public enum ItemType{HardCurrency,SoftCurrency,Bundle,None};
    public enum PurchaseType{HardCurrency,SoftCurrency,IAP,None};

    public enum VirtualCurrency{Gold,Gem,Coin,Chip,None}
    public ShoppingItem(){

    }

    public ShoppingItem(ConfigurableObject configurableObject){
        super(configurableObject);
    }

    public ShoppingItem(JsonObject payload){
        this.header = payload;
        this.configurationName = payload.get("Name").getAsString();
        this.distributionId = payload.get("ConfigurationId").getAsInt();
    }

    public int getFactoryId() {
        return ItemPortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return ItemPortableRegistry.SHOPPING_ITEM_CID;
    }

    public String name(){
        return configurationName();
    }

    public String skuName(){
        return header.get("SkuName").getAsString();
    }

    public ItemType itemType(){
        return toItemType(header.get("ItemType").getAsInt());
    }

    public PurchaseType purchaseType(){
        return toPurchaseType(header.get("PurchaseType").getAsInt());
    }

    public VirtualCurrency virtualCurrency(){
        return toVirtualCurrency(header.get("VirtualCurrency").getAsInt());
    }

    public double price(){
        return header.get("Price").getAsDouble();
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        JsonArray list = new JsonArray();
        if(_reference!=null) {
            _reference.forEach((cob)-> list.add(cob.toJson()));
        }
        json.add("itemList",list);
        return json;
    }

    private ItemType toItemType(int type){
        if(type==ItemType.HardCurrency.ordinal()) return ItemType.HardCurrency;
        if(type==ItemType.SoftCurrency.ordinal()) return ItemType.SoftCurrency;
        if(type==ItemType.Bundle.ordinal()) return ItemType.Bundle;
        return ItemType.None;
    }
    private PurchaseType toPurchaseType(int type){
        if(type==PurchaseType.HardCurrency.ordinal()) return PurchaseType.HardCurrency;
        if(type==PurchaseType.SoftCurrency.ordinal()) return PurchaseType.SoftCurrency;
        if(type==PurchaseType.IAP.ordinal()) return PurchaseType.IAP;
        return PurchaseType.None;
    }
    private VirtualCurrency toVirtualCurrency(int type){
        if(type==VirtualCurrency.Gold.ordinal()) return VirtualCurrency.Gold;
        if(type==VirtualCurrency.Gem.ordinal()) return VirtualCurrency.Gem;
        if(type==VirtualCurrency.Coin.ordinal()) return VirtualCurrency.Coin;
        if(type==VirtualCurrency.Chip.ordinal()) return VirtualCurrency.Chip;
        return VirtualCurrency.None;
    }

    public List<Commodity> commodityList(){
        ArrayList<Commodity> commodities = new ArrayList<>();
        header.get("_skuList").getAsJsonArray().forEach((e)->{
            JsonObject sku = e.getAsJsonObject().get("_sku").getAsJsonObject();
            commodities.add(Commodity.build(sku));
        });
        return commodities;
    }
}
