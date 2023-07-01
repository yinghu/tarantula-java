package com.tarantula.platform.inbox;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.inventory.Inventory;

import java.util.List;

public class Inbox extends RecoverableObject {

    public List<Inventory> inventoryList;
    public List<PendingReward> rewardList;


    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Successful",true);
        JsonArray inventories = new JsonArray();
        inventoryList.forEach((inventory -> inventories.add(inventory.toJson())));
        jsonObject.add("_inventoryList",inventories);
        JsonArray rewards = new JsonArray();
        rewardList.forEach((reward -> rewards.add(reward.toJson())));
        jsonObject.add("_rewardList",rewards);
        return jsonObject;
    }
}
