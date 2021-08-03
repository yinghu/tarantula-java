package com.tarantula.platform.tournament;

public class JoinTournament extends TournamentHeader {

    public JoinTournament(){

    }
    @Override
    public String register(String systemId){
        TournamentInstanceHeader instanceHeader = new TournamentInstanceHeader(maxEntriesPerInstance,startTime,closeTime,endTime);
        this.dataStore.create(instanceHeader);
        return instanceHeader.distributionKey();
    }
}
