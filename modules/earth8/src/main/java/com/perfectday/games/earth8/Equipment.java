package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;

import java.util.concurrent.ConcurrentHashMap;

public class Equipment extends GameItem{

    public enum Type{ Weapon,Shield,Helmet,Chest,Gloves,Boots}

    public Type type;
    public int rarity;
    public String primaryStatType;
    public int primaryStat;
    public ConcurrentHashMap<String,Integer> subStats;
    @Override
    public boolean write(DataBuffer buffer) {
        if(!super.write(buffer)) return false;
        buffer.writeInt(type.ordinal());
        buffer.writeInt(rarity);
        buffer.writeUTF8(primaryStatType);
        buffer.writeInt(primaryStat);
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        super.read(buffer);
        type = Type.values()[buffer.readInt()];
        rarity = buffer.readInt();
        primaryStatType = buffer.readUTF8();
        primaryStat = buffer.readInt();
        return true;
    }

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.EQUIPMENT_CID;
    }

    public static Equipment fromConfig(long itemId,Configurable configurable){
        Equipment equipment = new Equipment();
        equipment.distributionId(itemId);
        JsonObject header = configurable.header();
        equipment.type = Type.values()[header.get("Type").getAsInt()];
        equipment.configId = header.get("ConfigId").getAsString();
        equipment.level = header.get("Level").getAsInt();
        equipment.xp =  header.get("Xp").getAsInt();
        equipment.rank =  header.get("Rank").getAsInt();
        equipment.rarity =  header.get("Rarity").getAsInt();
        equipment.primaryStatType =  header.get("PrimaryStatType").getAsString();
        equipment.primaryStat =  header.get("PrimaryStat").getAsInt();
        return equipment;
    }
}
