package com.tarantula.platform.inbox;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GlobalItemGrantEvent extends RecoverableObject {
    public static final String LABEL = "inbox";

    public String itemName;
    public String itemID;
    public long amount;
    public LocalDateTime dateCreated;
    public long grantCount;

    public GlobalItemGrantEvent(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public GlobalItemGrantEvent(String itemName, String itemID, long amount, LocalDateTime dateCreated){
        this();
        this.itemName = itemName;
        this.itemID = itemID;
        this.amount = amount;
        this.dateCreated = dateCreated;
        this.grantCount = 0;
        this.name = "GlobalGrant--" + itemID + "--" + amount + "--" + dateCreated;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(name);
        buffer.writeUTF8(itemName);
        buffer.writeUTF8(itemID);
        buffer.writeLong(amount);
        buffer.writeLong(TimeUtil.toUTCMilliseconds(dateCreated));
        buffer.writeLong(grantCount);
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        name = buffer.readUTF8();
        itemName = buffer.readUTF8();
        itemID = buffer.readUTF8();
        amount = buffer.readLong();
        dateCreated = TimeUtil.fromUTCMilliseconds(buffer.readLong());
        grantCount = buffer.readLong();
        return true;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Name",name);
        jsonObject.addProperty("ItemName", itemName);
        jsonObject.addProperty("ItemID", itemID);
        jsonObject.addProperty("Amount", amount);
        jsonObject.addProperty("DateCreated", dateCreated.format(DateTimeFormatter.ISO_DATE_TIME));
        jsonObject.addProperty("GrantAmount", grantCount);
        return jsonObject;
    }
}