package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;
import com.icodesoftware.service.ServiceProvider;

public interface DistributionTournamentService extends ServiceProvider {

    String NAME = "DistributionTournamentService";

    boolean checkAvailable(String serviceName,String tournamentId);
    String join(String serviceName, String tournamentId, String systemId);
    byte[] enter(String serviceName,String tournamentId,String instanceId,String systemId);
    byte[] score(String serviceName,String instanceId,String systemId,double delta);
    byte[] schedule(String serviceName, Tournament.Schedule schedule);
    boolean localPartition(String distributionKey);

}
