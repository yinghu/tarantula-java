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

    boolean onScoreTournament(String serviceName,long tournamentId,long instanceId,long systemId,double credit,double delta);

    //global unlimited entries segmented instance
    long onEnterGlobalTournament(String serviceName,long tournamentId,long segmentInstanceId,long systemId);

    boolean onScoreGlobalTournament(String serviceName,long tournamentId,long instanceId,long entryId, long systemId,double credit,double delta);

    //shared
    byte[] onRaceBoard(String serviceName,long tournamentId,long instanceId);


    //management

    void onFinishTournament(String serviceName,String tournamentId,String instanceId,String systemId);

    void onSyncTournament(String serviceName,String tournamentId,String instanceId);
    void onCloseTournament(String serviceName,String tournamentId);
    void onEndTournament(String serviceName,long tournamentId);

}
