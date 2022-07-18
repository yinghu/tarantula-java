package com.tarantula.platform.tournament;

import com.icodesoftware.RecoverableFactory;


public class TournamentScheduleQuery implements RecoverableFactory<TournamentSchedule> {

    private String query;

    public TournamentScheduleQuery(String query){
        this.query = query;
    }

    public TournamentSchedule create() {
        TournamentSchedule ocx = new TournamentSchedule();
        return ocx;
    }

    public String distributionKey() {
        return this.query;
    }


    public  int registryId(){
        return TournamentPortableRegistry.TOURNAMENT_SCHEDULE_CID;
    }

    public String label(){
        return query;
    }
}
