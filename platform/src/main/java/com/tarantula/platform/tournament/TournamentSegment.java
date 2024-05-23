package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TournamentSegment {

    public TournamentInstance tournamentInstance;

    private ConcurrentHashMap<Long,TournamentEntry> playerIndex = new ConcurrentHashMap<>();
    private List<TournamentEntry> snapshot = new ArrayList<>();

    public void snapshot(){
        int rank = 1;
        List<TournamentEntry> sorted = tournamentInstance.sorted();
        for(TournamentEntry entry : sorted){
            entry.rank(rank++);
            playerIndex.put(entry.systemId(),entry);
        }
        snapshot = sorted;
    }

    public Tournament.RaceBoard myRaceBoard(long systemId){
        TournamentEntry me = playerIndex.get(systemId);
        if(me==null) return new TournamentRaceBoard();
        return null;
    }
}
