package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.Tournament;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.TarantulaContext;

import java.util.Properties;

public class TournamentClusterService implements ManagedService, RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(TournamentClusterService.class);

    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        this.tarantulaContext = TarantulaContext.getInstance();
        log.warn("Start tournament cluster service");
    }

    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {
        log.warn("Shutting down tournament cluster service");
    }

    @Override
    public DistributedObject createDistributedObject(String objectName) {
        return new DistributionTournamentServiceProxy(objectName,nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String objectName) {
        log.warn(objectName+" destroyed");
    }


    public boolean enter(String serviceName,long tournamentId,long systemId){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        return tsp.tournamentServiceProvider().onTournamentEntered(tournamentId,systemId);
    }

    public Tournament.Instance join(String serviceName,long tournamentId,long instanceId,long systemId){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        Tournament.Instance _ins = tsp.tournamentServiceProvider().onTournamentEntered(tournamentId,instanceId,systemId);
        return _ins;
    }
    public Tournament.Entry score(String serviceName, long tournamentId,long instanceId, long systemId,double credit,double delta){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        return tsp.tournamentServiceProvider().onTournamentScored(tournamentId,instanceId,systemId,credit,delta);
    }
    public Tournament.RaceBoard list(String serviceName,long tournamentId,long instanceId){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        return tsp.tournamentServiceProvider().onTournamentListed(tournamentId,instanceId);
    }
    public Tournament.RaceBoard list(String serviceName,long tournamentId){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        return tsp.tournamentServiceProvider().onTournamentListed(tournamentId);
    }
    public void finish(String serviceName,String tournamentId, String instanceId, String systemId){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        tsp.tournamentServiceProvider().onTournamentFinished(tournamentId,instanceId,systemId);
    }

    public void syncTournament(String serviceName,String tournamentId,String instanceId){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        tsp.tournamentServiceProvider().onTournamentSynced(tournamentId,instanceId);
    }

    public void closeTournament(String serviceName,String tournamentId){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        tsp.tournamentServiceProvider().onTournamentClosed(tournamentId);
    }

    public void endTournament(String serviceName,String tournamentId){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        tsp.tournamentServiceProvider().onTournamentEnded(tournamentId);
    }
}
