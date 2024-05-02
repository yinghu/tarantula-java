package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.util.JsonUtil;
import com.perfectday.games.earth8.analytics.AnalyticsEquipmentData;
import com.perfectday.games.earth8.analytics.EquipmentLevelUpTransaction;
import com.perfectday.games.earth8.analytics.EquipmentXpUpTransaction;

public class EquipmentXpUp extends BattleUpdate{

    private AnalyticsEquipmentData _equipmentData;

    public int xpGain;
    public int fromLevel;
    public int toLevel;

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
        return Earth8PortableRegistry.EQUIPMENT_XP_UP_CID;
    }


    public static EquipmentXpUp fromJson(JsonObject jsonObject){
        EquipmentXpUp self = new EquipmentXpUp();
        self.parse(jsonObject);
        self.xpGain = JsonUtil.getJsonInt(jsonObject, "XpGain", 0);
        self.fromLevel = JsonUtil.getJsonInt(jsonObject, "FromLevel", 0);
        self.toLevel = JsonUtil.getJsonInt(jsonObject, "ToLevel", 0);
        self._equipmentData = new AnalyticsEquipmentData(jsonObject);
        return self;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session,long serverSessionId,long batchId){
        pendingAnalytics.add(new EquipmentXpUpTransaction(session,serverSessionId, _equipmentData, equipmentId, xpGain));
        if(toLevel > fromLevel)
        {
            pendingAnalytics.add(new EquipmentLevelUpTransaction(session,serverSessionId, _equipmentData, equipmentId, fromLevel, toLevel));
        }
        return true;
    }

}
