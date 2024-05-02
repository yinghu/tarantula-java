package com.perfectday.games.earth8;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.util.JsonUtil;
import com.perfectday.games.earth8.analytics.UnitRankUpTransaction;

public class UnitRankUp extends BattleUpdate{

    public int rank;
    private String _unitName;

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
        return Earth8PortableRegistry.UNIT_RANK_UP_CID;
    }


    public static UnitRankUp fromJson(JsonObject jsonObject){
        UnitRankUp self = new UnitRankUp();
        self.parse(jsonObject);
        self.rank = JsonUtil.getJsonInt(jsonObject, "Rank", 0);
        self._unitName = JsonUtil.getJsonString(jsonObject, "TEMP_UnitName", "");
        return self;
    }

    @Override
    protected boolean runUpdate(ApplicationPreSetup applicationPreSetup, Session session,long serverSessionId,long batchId){
        pendingAnalytics.add(new UnitRankUpTransaction(session,serverSessionId, unitId, rank, _unitName));
        return true;
    }
}
