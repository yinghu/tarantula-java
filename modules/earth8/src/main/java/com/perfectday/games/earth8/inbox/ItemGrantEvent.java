package com.perfectday.games.earth8.inbox;

import com.google.gson.JsonObject;
import com.icodesoftware.OnAccess;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.perfectday.games.earth8.Earth8PortableRegistry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ItemGrantEvent extends RecoverableObject implements OnAccess {
    public static final String LABEL = "inbox";

    public String type;
    public String itemID;
    public String itemName;

    public int amount;

    public boolean completed;

    public LocalDateTime dateCreated;


    public ItemGrantEvent(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public ItemGrantEvent(String type, String itemID, String itemName, int amount, boolean completed, LocalDateTime dateCreated){
        this();
        this.type = type;
        this.itemID = itemID;
        this.itemName = itemName;
        this.amount = amount;
        this.completed = completed;
        this.dateCreated = dateCreated;
    }

    @Override
    public int getFactoryId() {
        return Earth8PortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return Earth8PortableRegistry.ITEM_GRANT_EVENT_CID;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(type);
        buffer.writeUTF8(itemID);
        buffer.writeUTF8(itemName);
        buffer.writeInt(amount);
        buffer.writeBoolean(completed);
        buffer.writeLong(TimeUtil.toUTCMilliseconds(dateCreated));
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        type = buffer.readUTF8();
        itemID = buffer.readUTF8();
        itemName = buffer.readUTF8();
        amount = buffer.readInt();
        completed = buffer.readBoolean();
        dateCreated = TimeUtil.fromUTCMilliseconds(buffer.readLong());
        return true;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Type",type);
        jsonObject.addProperty("ItemID",itemID);
        jsonObject.addProperty("ItemName",itemName);
        jsonObject.addProperty("Amount",amount);
        jsonObject.addProperty("Completed",completed);
        jsonObject.addProperty("DateCreated", dateCreated.format(DateTimeFormatter.ISO_DATE_TIME));
        return jsonObject;
    }
    @Override
    public String toString(){
        return "Type: " + type + " ItemName: " + itemName + " Amount: " + amount + " Completed: " + completed;
    }

    @Override
    public String systemId() {
        return null;
    }
    @Override
    public void systemId(String systemId) {}
    @Override
    public long stub() {
        return 0;
    }
    @Override
    public void stub(long stub) {}
    @Override
    public long tournamentId() {
        return 0;
    }
    @Override
    public void tournamentId(long tournamentId) {}
    @Override
    public String typeId() {
        return null;
    }
    @Override
    public void typeId(String typeId) {}
    @Override
    public String ticket() {
        return null;
    }
    @Override
    public void ticket(String ticket) {}
    @Override
    public String command() {
        return null;
    }
    @Override
    public void command(String command) {}
    @Override
    public int code() {
        return 0;
    }
    @Override
    public void code(int code) {}
    @Override
    public String message() {
        return null;
    }
    @Override
    public void message(String message) {}
    @Override
    public boolean successful() {
        return false;
    }
    @Override
    public void successful(boolean successful) {}
}
