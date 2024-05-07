package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.service.ServiceProvider;

public interface DistributionTournamentService extends ServiceProvider {

    String NAME = "DistributionTournamentService";

    boolean ownership(long tournamentId);
    int partitionId(long tournamentId);

    //limited entries instance base
    TournamentRegisterStatus onRegisterTournament(String serviceName,long tournamentId,int slot);

    Tournament.Instance onEnterTournament(String serviceName,long tournamentId,long instanceId, long systemId);

    double onScoreTournament(String serviceName,long tournamentId,long instanceId,long systemId,double credit,double delta);

    //global unlimited entries segmented instance
    long onEnterGlobalTournament(String serviceName,long tournamentId,long segmentInstanceId,long systemId);

    double onScoreGlobalTournament(String serviceName,long tournamentId,long instanceId,long entryId, long systemId,double credit,double delta);

    //shared
    byte[] onRaceBoard(String serviceName,long tournamentId,long instanceId);
    byte[] onMyRaceBoard(String serviceName,long tournamentId,long instanceId,long entryId,long systemId);

    //management
    void onEndTournament(String serviceName,long tournamentId);

}
