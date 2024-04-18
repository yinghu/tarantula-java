package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.perfectday.games.earth8.analytics.AnalyticsEquipmentData;
import com.perfectday.games.earth8.analytics.EquipmentRankUpTransaction;

public class EquipmentRankUp extends BattleUpdate{

    private AnalyticsEquipmentData _equipmentData;

    public int rank;
    @Override
    public boolean write(DataBuffer buffer) {
        if(!super.write(buffer)) return false;
        buffer.writeInt(rank);
        return true;
    }

    //Data store read contract
    @Override
    public boolean read(DataBuffer buffer) {
        super.read(buffer);
        rank = buffer.readInt();
        return true;
    }


    @Override
    public int getClassId() {
        return Earth8PortableRegistry.EQUIPMENT_RANK_UP_CID;
    }


    public static EquipmentRankUp fromJson(JsonObject jsonObject){
        EquipmentRankUp self = new EquipmentRankUp();
        self.parse(jsonObject);
        self.rank = jsonObject.get("Rank").getAsInt();
        self._equipmentData = new AnalyticsEquipmentData(jsonObject);
        return self;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session,long serverSessionId,long batchId){
        pendingAnalytics.add(new EquipmentRankUpTransaction(session,serverSessionId, _equipmentData, equipmentId, rank));
        return true;
    }

}
