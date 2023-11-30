package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.service.ServiceProvider;

public interface DistributionTournamentService extends ServiceProvider {

    String NAME = "DistributionTournamentService";

    boolean onEnterTournament(String serviceName,long tournamentId,long systemId);
    Tournament.Instance onEnterTournament(String serviceName,long tournamentId,long instanceId, long systemId);
    Tournament.Entry onScoreTournament(String serviceName,long tournamentId,long instanceId,long systemId,double credit,double delta);

    Tournament.RaceBoard onListTournament(String serviceName,long tournamentId,long instanceId);
    Tournament.RaceBoard onListTournament(String serviceName,long tournamentId);

    void onFinishTournament(String serviceName,String tournamentId,String instanceId,String systemId);

    void onSyncTournament(String serviceName,String tournamentId,String instanceId);
    void onCloseTournament(String serviceName,String tournamentId);
    void onEndTournament(String serviceName,String tournamentId);

}
