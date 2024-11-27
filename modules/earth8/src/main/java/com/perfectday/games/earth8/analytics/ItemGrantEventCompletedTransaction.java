package com.perfectday.games.earth8.analytics;

import com.icodesoftware.Session;

import java.time.LocalDateTime;

public class ItemGrantEventCompletedTransaction extends UserAnalyticsTransaction{

    private static final String MESSAGE_TYPE = "/earth8/player/0.0.1/itemGrantEvent";

    public ItemGrantEventCompletedTransaction(Session session, long serverSessionId, String type, String itemName, int amount, LocalDateTime dateCreated, LocalDateTime dateCompleted)
    {
        super(MESSAGE_TYPE, session,serverSessionId);
        data.addProperty("type", type);
        data.addProperty("itemName", itemName);
        data.addProperty("amount", amount);
        data.addProperty("date_created", dateCreated.toString());
        data.addProperty("date_completed", dateCompleted.toString());
    }
}
