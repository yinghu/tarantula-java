package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class TournamentScheduleStatus extends RecoverableObject {


    public Tournament.Status status = Tournament.Status.PENDING;//-> STARTING -> STARTED -> ENDED
    @Override
    public Map<String,Object> toMap(){
        properties.put("index",index);
        properties.put("status",status.name());
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.index = (String)properties.get("index");
        this.status = Tournament.Status.valueOf((String)properties.get("status"));
    }

    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }
    public int getClassId() {
        return TournamentPortableRegistry.TOURNAMENT_SCHEDULE_STATUS_CID;
    }

    public String toString(){
        return "Tournament ["+index+"]["+status+"]";
    }
}
