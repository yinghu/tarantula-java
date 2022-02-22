package com.tarantula.platform.inbox;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.inventory.Inventory;

import java.util.List;

public class Inbox extends RecoverableObject {

    public List<Inventory> inventoryList;

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        JsonArray inventories = new JsonArray();
        inventoryList.forEach((inventory -> inventories.add(inventory.toJson())));
        jsonObject.add("inventoryList",inventories);
        return jsonObject;
    }
}
