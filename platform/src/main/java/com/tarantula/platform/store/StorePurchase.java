package com.tarantula.platform.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Inventory;
import com.icodesoftware.util.ResponseHeader;
import java.util.List;
import java.util.Map;

public class StorePurchase extends ResponseHeader {

    public String transactionId;
    public List<Inventory> inventoryList;
    public boolean isSandbox;

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("transactionId",transactionId);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.transactionId = (String) properties.getOrDefault("transactionId","n/a");
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",true);
        jsonObject.addProperty("TransactionId",transactionId);
        jsonObject.addProperty("IsSandbox", isSandbox);
        JsonArray inventories = new JsonArray();
        inventoryList.forEach((inventory -> inventories.add(inventory.toJson())));
        jsonObject.add("_inventoryList",inventories);
        return jsonObject;
    }
}
