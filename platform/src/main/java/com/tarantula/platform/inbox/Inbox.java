package com.tarantula.platform.inbox;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.icodesoftware.Inventory;
import com.icodesoftware.protocol.OnInbox;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.achievement.AchievementItem;

import com.tarantula.platform.presence.dailygiveaway.DailyGiveaway;
import com.tarantula.platform.store.Shop;

import java.util.List;

public class Inbox extends RecoverableObject {

    public Shop shop;
    public List<Inventory> inventoryList;
    public List<PendingReward> rewardList;

    public List<AchievementItem> achievementList;

    public List<DailyGiveaway> dailyGiveawayList;

    public List<OnInbox> inboxList;


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

        jsonObject.add("_shop",shop.toJson());

        //OnInbox list
        JsonArray dataList = new JsonArray();
        inboxList.forEach(inbox->{
            JsonObject data = new JsonObject();
            data.addProperty("Name",inbox.name());
            data.addProperty("Category",inbox.name());
            JsonArray content = new JsonArray();
            inbox.content().forEach((v)->content.add(v.toJson()));
            data.add("_content",content);
            dataList.add(data);
        });
        jsonObject.add("_onInbox",dataList);
        return jsonObject;
    }
}
