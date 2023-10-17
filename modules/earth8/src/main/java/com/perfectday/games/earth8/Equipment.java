package com.perfectday.games.earth8;

import java.util.concurrent.ConcurrentHashMap;

public class Equipment extends GameItem{
    public int rarity;
    public String primaryStatType;
    public int primaryStat;
    public ConcurrentHashMap<String,Integer> subStats;
    @Override
    public boolean write(DataBuffer buffer) {
        if(!super.write(buffer)) return false;
        buffer.writeInt(rarity);
        buffer.writeUTF8(primaryStatType);
        buffer.writeInt(primaryStat);
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        super.read(buffer);
        rarity = buffer.readInt();
        primaryStatType = buffer.readUTF8();
        primaryStat = buffer.readInt();
        return true;
    }

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.EQUIPMENT_CID;
    }
}
