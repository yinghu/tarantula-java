package com.tarantula.platform.service.cluster;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.Tournament;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.TournamentServiceProvider;
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
        TournamentServiceProvider tsp = (TournamentServiceProvider) tarantulaContext.serviceProvider(serviceName);
        return tsp.tournament(tournamentId)!=null;
    }
    public String join(String serviceName,String tournamentId,String systemId){
        TournamentServiceProvider tsp = (TournamentServiceProvider) tarantulaContext.serviceProvider(serviceName);
        Tournament.Instance _ins = tsp.tournament(tournamentId).join(systemId);
        return _ins.id();
    }
    public double score(String serviceName,String instanceId,String systemId,double delta){
        TournamentServiceProvider tsp = (TournamentServiceProvider) tarantulaContext.serviceProvider(serviceName);
        Tournament.Instance _ins = tsp.instance(instanceId);
        double[] score={0};
        _ins.update(systemId,(e)->{
            score[0]=e.score(delta);
        });
        return score[0];
    }
}
