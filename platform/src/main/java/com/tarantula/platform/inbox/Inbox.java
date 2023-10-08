package com.tarantula.platform.inbox;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Inventory;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.achievement.Achievement;
import com.tarantula.platform.presence.dailygiveaway.DailyGiveaway;
import com.tarantula.platform.store.Shop;
import com.tarantula.platform.store.ShoppingItem;

import java.util.List;

public class Inbox extends RecoverableObject {

    public Shop shop;
    public List<Inventory> inventoryList;
    public List<PendingReward> rewardList;

    public List<Achievement> achievementList;

    public List<DailyGiveaway> dailyGiveawayList;
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

        JsonArray achievements = new JsonArray();
        achievementList.forEach((v)->achievements.add(v.toJson()));
        jsonObject.add("_achievementList",achievements);

        JsonArray dailyGiveaways = new JsonArray();
        dailyGiveawayList.forEach((v)->dailyGiveaways.add(v.toJson()));
        jsonObject.add("_dailyGiveawayList",dailyGiveaways);

        //JsonArray shoppingItems = new JsonArray();
        //shoppingItemList.forEach((v)->shoppingItems.add(v.toJson()));
        jsonObject.add("_shop",shop.toJson());

        return jsonObject;
    }
}
