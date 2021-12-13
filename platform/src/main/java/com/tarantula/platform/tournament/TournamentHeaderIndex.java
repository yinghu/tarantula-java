package com.tarantula.platform.tournament;

public class TournamentHeaderIndex {

    public TournamentHeader tournamentHeader;
    public int partitionId;
    public boolean localManaged;

    public TournamentHeaderIndex(int partitionId,boolean localManaged){
        this.partitionId = partitionId;
        this.localManaged = localManaged;
    }

    @Override
    public String toString(){
        return "Tournament->"+tournamentHeader.distributionKey()+">>"+localManaged+">>>"+partitionId;
    }
}
