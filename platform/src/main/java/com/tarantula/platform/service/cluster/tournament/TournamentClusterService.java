package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.Tournament;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.game.service.GameServiceProvider;
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

    public boolean checkAvailable(String serviceName,String tournamentId){
        GameServiceProvider tsp = (GameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        return tsp.onTournament(tournamentId)!=null;
    }
    public String register(String serviceName,String tournamentId,String systemId){
        GameServiceProvider tsp = (GameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        return tsp.onTournament(tournamentId).register(systemId);
    }
    public Tournament.Instance join(String serviceName,String tournamentId,String instanceId,String systemId){
        GameServiceProvider tsp = (GameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        Tournament.Instance _ins = tsp.onInstance(tournamentId,instanceId);
        _ins.join(systemId);
        return _ins;
    }
    public Tournament.Entry score(String serviceName, String instanceId, String systemId, double delta){
        GameServiceProvider tsp = (GameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        Tournament.Instance _ins = tsp.onInstance(instanceId);
        Tournament.Entry[] score={null};
        _ins.update(systemId,(e)->{
            e.score(delta);
            score[0]=e;
        });
        return score[0];
    }
    public Tournament.Entry configure(String serviceName, String instanceId, String systemId, byte[] payload){
        GameServiceProvider tsp = (GameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        Tournament.Instance _ins = tsp.onInstance(instanceId);
        Tournament.Entry[] score={null};
        _ins.update(systemId,(e)->{
            if(e.configureAndValidate(payload)){
                score[0]=e;
            }
        });
        return score[0];
    }
    public Tournament.RaceBoard list(String serviceName,String instanceId){
        GameServiceProvider tsp = (GameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        return tsp.onRaceBoard(instanceId);
    }

    public void closeTournament(String serviceName,String tournamentId){
        GameServiceProvider tsp = (GameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        tsp.onCloseTournament(tournamentId);
    }

    public void endTournament(String serviceName,String tournamentId){
        GameServiceProvider tsp = (GameServiceProvider) tarantulaContext.serviceProvider(serviceName);
        tsp.onEndTournament(tournamentId);
    }
}
