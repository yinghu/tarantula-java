package com.tarantula.platform.inbox;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GlobalItemGrantEvent extends RecoverableObject {
    public static final String LABEL = "inbox";

    public boolean completed;
    public String itemName;
    public String itemID;
    public int amount;
    public LocalDateTime dateCreated;

    public int minPlayerLevelFilter;
    public int maxPlayerLevelFilter;
    public LocalDate minInstallDateFilter;
    public LocalDate maxInstallDateFilter;
    public long tournamentIdFilter;

    public GlobalItemGrantEvent(){
        this.onEdge = true;
        this.label = LABEL;
    }

    public GlobalItemGrantEvent(String itemName, String itemID, int amount, LocalDateTime dateCreated){
        this();
        this.completed = false;
        this.itemName = itemName;
        this.itemID = itemID;
        this.amount = amount;
        this.dateCreated = dateCreated;
        this.minPlayerLevelFilter = Integer.MIN_VALUE;
        this.maxPlayerLevelFilter = Integer.MAX_VALUE;
        this.minInstallDateFilter = LocalDate.parse("1000-01-01");
        this.maxInstallDateFilter = LocalDate.parse("5000-01-01");
        this.tournamentIdFilter = 0;
        this.name = "GlobalGrant--" + itemID + "--" + amount + "--" + dateCreated;
    }

    public void setPlayerLevelFilter(int minPlayerLevelFilter, int maxPlayerLevelFilter){
        this.minPlayerLevelFilter = minPlayerLevelFilter;
        this.maxPlayerLevelFilter = maxPlayerLevelFilter;
    }

    public void setInstallDateFilter(LocalDate minInstallDateFilter, LocalDate maxInstallDateFilter){
        this.minInstallDateFilter = minInstallDateFilter;
        this.maxInstallDateFilter = maxInstallDateFilter;
    }

    public void setTournamentIdFilter(long tournamentId){
        this.tournamentIdFilter = tournamentId;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    @Override
    public boolean write(DataBuffer buffer) {
        buffer.writeUTF8(name);
        buffer.writeUTF8(itemName);
        buffer.writeUTF8(itemID);
        buffer.writeInt(amount);
        buffer.writeLong(TimeUtil.toUTCMilliseconds(dateCreated));
        buffer.writeInt(minPlayerLevelFilter);
        buffer.writeInt(maxPlayerLevelFilter);
        buffer.writeLong(TimeUtil.toUTCMilliseconds(minInstallDateFilter.atStartOfDay()));
        buffer.writeLong(TimeUtil.toUTCMilliseconds(maxInstallDateFilter.atStartOfDay()));
        buffer.writeLong(tournamentIdFilter);
        buffer.writeBoolean(completed);
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        name = buffer.readUTF8();
        itemName = buffer.readUTF8();
        itemID = buffer.readUTF8();
        amount = buffer.readInt();
        dateCreated = TimeUtil.fromUTCMilliseconds(buffer.readLong());
        minPlayerLevelFilter = buffer.readInt();
        maxPlayerLevelFilter = buffer.readInt();
        minInstallDateFilter = TimeUtil.fromUTCMilliseconds(buffer.readLong()).toLocalDate();
        maxInstallDateFilter = TimeUtil.fromUTCMilliseconds(buffer.readLong()).toLocalDate();
        tournamentIdFilter = buffer.readLong();
        completed = buffer.readBoolean();
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
        jsonObject.addProperty("MinPlayerLevelFilter", minPlayerLevelFilter);
        jsonObject.addProperty("MaxPlayerLevelFilter", maxPlayerLevelFilter);
        if(minInstallDateFilter != null) jsonObject.addProperty("MinInstallDateFilter", minInstallDateFilter.toString());
        if(maxInstallDateFilter != null) jsonObject.addProperty("MaxInstallDateFilter", maxInstallDateFilter.toString());
        jsonObject.addProperty("TournamentIdFilter", tournamentIdFilter);
        jsonObject.addProperty("Completed", completed);

        return jsonObject;
    }
}