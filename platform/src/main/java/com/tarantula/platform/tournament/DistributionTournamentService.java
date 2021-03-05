package com.tarantula.platform.tournament;

import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.service.Instance;

public interface DistributionTournamentService extends ServiceProvider {

    String NAME = "DistributionTournamentService";

    boolean checkAvailable(String serviceName,String tournamentId);
    String join(String serviceName,String tournamentId,String systemId);
    double score(String serviceName,String instanceId,String systemId,double delta);

}
