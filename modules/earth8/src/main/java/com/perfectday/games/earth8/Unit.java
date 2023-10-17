package com.perfectday.games.earth8;

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

}
