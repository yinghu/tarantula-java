package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.service.ServiceProvider;

public interface DistributionTournamentService extends ServiceProvider {

    String NAME = "DistributionTournamentService";


    Tournament.Instance onEnterTournament(String serviceName,String tournamentId,String instanceId,String systemId);
    Tournament.Entry onScoreTournament(String serviceName,String tournamentId,String instanceId,String systemId,double delta);

    Tournament.RaceBoard onListTournament(String serviceName,String tournamentId,String instanceId);
    void onFinishTournament(String serviceName,String tournamentId,String instanceId,String systemId);

    void onSyncTournament(String serviceName,String tournamentId,String instanceId);
    void onCloseTournament(String serviceName,String tournamentId);
    void onEndTournament(String serviceName,String tournamentId);

}
