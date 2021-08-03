package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;

public class PlayTournament extends TournamentHeader{

    public Tournament.Instance findInstance(String instanceId){
        TournamentInstanceHeader instanceHeader = new TournamentInstanceHeader();
        instanceHeader.distributionKey(instanceId);
        dataStore.load(instanceHeader);
        return instanceHeader;
    }
}
