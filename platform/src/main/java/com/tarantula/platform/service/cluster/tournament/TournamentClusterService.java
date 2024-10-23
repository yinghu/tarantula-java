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
import com.tarantula.platform.tournament.TournamentRegisterStatus;

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

    public TournamentRegisterStatus register(String serviceName, long tournamentId, int slot){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        return tsp.tournamentServiceProvider().onTournamentRegistered(tournamentId,slot);
    }

    public long joinOnSegment(String serviceName,long tournamentId,long segmentInstanceId,long systemId){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        return tsp.tournamentServiceProvider().onTournamentSegmentEntered(tournamentId,segmentInstanceId,systemId);
    }

    public double scoreOnSegment(String serviceName, long tournamentId,long instanceId, long entryId,long systemId,double credit,double delta){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        return tsp.tournamentServiceProvider().onTournamentSegmentScored(tournamentId,instanceId,entryId,systemId,credit,delta);
    }

    public Tournament.Instance join(String serviceName,long tournamentId,long instanceId,long systemId){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        Tournament.Instance _ins = tsp.tournamentServiceProvider().onTournamentEntered(tournamentId,instanceId,systemId);
        return _ins;
    }

    public double score(String serviceName, long tournamentId,long instanceId, long systemId,double credit,double delta){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        return tsp.tournamentServiceProvider().onTournamentScored(tournamentId,instanceId,systemId,credit,delta);
    }

    public byte[] raceBoard(String serviceName,long tournamentId,long instanceId){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        return tsp.tournamentServiceProvider().onTournamentRaceBoardListed(tournamentId,instanceId);
    }

    public byte[] myRaceBoard(String serviceName,long tournamentId,long instanceId,long entryId,long systemId){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        return tsp.tournamentServiceProvider().onTournamentMyRaceBoardListed(tournamentId,instanceId,entryId,systemId);
    }

    public void endTournament(String serviceName,long tournamentId){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        tsp.tournamentServiceProvider().onTournamentEnded(tournamentId);
    }

    public byte[] scan(String serviceName){
        PlatformGameServiceProvider tsp = (PlatformGameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        return tsp.tournamentServiceProvider().onTournamentScanned();
    }
}
