package com.tarantula.platform.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tarantula.platform.item.*;


public class ShoppingItem extends Item{


    public enum ItemType{HardCurrency,SoftCurrency,Bundle};
    public enum PurchaseType{HardCurrency,SoftCurrency,IAP};
    public ShoppingItem(){

    }

    public ShoppingItem(ConfigurableObject configurableObject){
        super(configurableObject);
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
        return ItemType.valueOf(header.get("ItemType").getAsString());
    }

    public PurchaseType purchaseType(){
        return PurchaseType.valueOf(header.get("PurchaseType").getAsString());
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

}
