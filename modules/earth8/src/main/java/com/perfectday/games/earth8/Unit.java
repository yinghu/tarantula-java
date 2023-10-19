package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;

import java.util.List;

public class Unit extends GameItem{

    public List<Equipment> equipmentList;
    public List<String> currencyConstraint;

    @Override
    public boolean write(DataBuffer buffer) {
        if(!super.write(buffer)) return false;
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        super.read(buffer);
        return true;
    }

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.UNIT_CID;
    }

    public static Unit fromConfig(long itemId,Configurable configurable){
        Unit unit = new Unit();
        unit.distributionId(itemId);
        JsonObject header = configurable.header();
        unit.configId = header.get("ConfigId").getAsString();
        unit.level = header.get("Level").getAsInt();
        unit.xp =  header.get("Xp").getAsInt();
        unit.rank =  header.get("Rank").getAsInt();
        return unit;
    }

}
