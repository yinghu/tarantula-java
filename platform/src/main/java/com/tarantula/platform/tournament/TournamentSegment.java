package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TournamentSegment {

    public TournamentInstance tournamentInstance;

    private final int topListSize;
    private final int myRaceBoardAheadNumber;
    private final int myRaceBoardBehindNumber;

    public TournamentSegment(PlatformTournamentServiceProvider tournamentServiceProvider){
        this.topListSize = tournamentServiceProvider.topRaceBoardSize;
        this.myRaceBoardAheadNumber = tournamentServiceProvider.myRaceBoardAheadNumber;
        this.myRaceBoardBehindNumber = tournamentServiceProvider.myRaceBoardBehindNumber;
    }

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

    public TournamentRaceBoard myRaceBoard(long systemId,long entryId){
        TournamentEntry me = playerIndex.get(systemId);
        if(me==null || me.distributionId() != entryId ) return new TournamentRaceBoard();
        List<Tournament.Entry> board = new ArrayList<>();
        for(int retry =0; retry<2;retry++){
            boolean passed = true;
            try{
                for(int rank = me.rank() - myRaceBoardAheadNumber; rank < me.rank(); rank++){
                    if(rank-1>=0 && rank-1<snapshot.size()) board.add(snapshot.get(rank-1));
                }
                board.add(me);
                for(int rank = me.rank()+1; rank <= me.rank()+myRaceBoardBehindNumber; rank++){
                    if(rank-1>=0 && rank-1<snapshot.size()) board.add(snapshot.get(rank-1));
                }
            }catch (Exception ex){
                //ignore when snapshot reassigned;
                passed = false;
                board.clear();
            }
            if(passed) break;
        }
        return new TournamentRaceBoard(board);
    }

    public Tournament.RaceBoard topList(){
        List<Tournament.Entry> board = new ArrayList<>();

        for(int retry =0; retry<2;retry++){
            boolean passed = true;
            try{
                for(int i=0;i<topListSize;i++){
                    if(i<snapshot.size()) board.add(snapshot.get(i));
                }
            }catch (Exception ex){
                //ignore when snapshot reassigned;
                passed = false;
                board.clear();
            }
            if(passed) break;
        }
        return new TournamentRaceBoard(board);
    }
}
