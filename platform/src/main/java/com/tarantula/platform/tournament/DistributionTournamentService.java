package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.service.ServiceProvider;

public interface DistributionTournamentService extends ServiceProvider {

    String NAME = "DistributionTournamentService";

    boolean checkAvailable(String serviceName,String tournamentId);
    String register(String serviceName, String tournamentId, String systemId);
    byte[] join(String serviceName,String tournamentId,String instanceId,String systemId);
    Tournament.Entry score(String serviceName,String instanceId,String systemId,double delta);
    Tournament.Entry configure(String serviceName,String instanceId,String systemId,byte[] payload);
    byte[] schedule(String serviceName, Tournament.Schedule schedule);

}
