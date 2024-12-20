package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.util.JsonUtil;
import com.perfectday.games.earth8.analytics.CampaignProgressTransaction;
import com.perfectday.games.earth8.analytics.ItemGrantEventCompletedTransaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ItemGrantEventCompleted extends BattleUpdate{
    public String type;
    public String itemID;

    public String itemName;

    public int amount;
    public LocalDateTime dateCreated;

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.ITEM_GRANT_EVENT_CID;
    }

    public static ItemGrantEventCompleted fromJson(JsonObject jsonObject){
        ItemGrantEventCompleted itemGrantEvent = new ItemGrantEventCompleted();
        itemGrantEvent.parse(jsonObject);
        itemGrantEvent.type = JsonUtil.getJsonString(jsonObject, "type", "");
        itemGrantEvent.itemID = JsonUtil.getJsonString(jsonObject, "itemID", "");
        itemGrantEvent.itemName = JsonUtil.getJsonString(jsonObject, "itemName", "");
        itemGrantEvent.amount = JsonUtil.getJsonInt(jsonObject, "amount", 0);
        itemGrantEvent.dateCreated = LocalDateTime.parse(JsonUtil.getJsonString(jsonObject, "dateCreated", LocalDateTime.MIN.format(DateTimeFormatter.ISO_DATE_TIME)), DateTimeFormatter.ISO_DATE_TIME);

        return itemGrantEvent;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session, long serverSessionId, long batchId) {
        pendingAnalytics.add(new ItemGrantEventCompletedTransaction(session,serverSessionId, type, itemName, amount, dateCreated, LocalDateTime.now()));
        return true;
    }
}

