package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.util.JsonUtil;
import com.perfectday.games.earth8.analytics.UnitLevelUpTransaction;
import com.perfectday.games.earth8.analytics.UnitXpUpTransaction;

public class UnitXpUp extends BattleUpdate{

    public int xpGain;
    public int fromLevel;
    public int toLevel;

    private String _unitName;

    @Override
    public boolean write(DataBuffer buffer) {
        if(!super.write(buffer)) return false;
        buffer.writeInt(xpGain);
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        super.read(buffer);
        xpGain = buffer.readInt();
        return true;
    }

    @Override
    public int getClassId() {
        return Earth8PortableRegistry.UNIT_XP_UP_CID;
    }


    public static UnitXpUp fromJson(JsonObject jsonObject){
        UnitXpUp self = new UnitXpUp();
        self.parse(jsonObject);
        self.xpGain = JsonUtil.getJsonInt(jsonObject, "XpGain", 0);
        self.fromLevel = JsonUtil.getJsonInt(jsonObject, "FromLevel", 0);
        self.toLevel = JsonUtil.getJsonInt(jsonObject, "ToLevel", 0);

        self._unitName = JsonUtil.getJsonString(jsonObject, "TEMP_UnitName", "");
        return self;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session){
        pendingAnalytics.add(new UnitXpUpTransaction(session, unitId, xpGain, _unitName));
        if(toLevel > fromLevel)
        {
            pendingAnalytics.add(new UnitLevelUpTransaction(session, unitId, fromLevel, toLevel, _unitName));
        }
        return true;
    }
}
