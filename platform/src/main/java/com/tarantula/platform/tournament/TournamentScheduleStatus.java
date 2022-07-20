package com.tarantula.platform.tournament;

import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class TournamentScheduleStatus extends RecoverableObject {


    @Override
    public Map<String,Object> toMap(){
        properties.put("index",index);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.index = (String)properties.get("index");
    }

    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_SCHEDULE_STATUS_CID;
    }
}
