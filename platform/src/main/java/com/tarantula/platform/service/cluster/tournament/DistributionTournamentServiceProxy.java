package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.Tournament;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.tournament.DistributionTournamentService;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DistributionTournamentServiceProxy extends AbstractDistributedObject<TournamentClusterService> implements DistributionTournamentService {

    private String objectName;

    public DistributionTournamentServiceProxy(String objectName, NodeEngine nodeEngine, TournamentClusterService tournamentClusterService){
        super(nodeEngine,tournamentClusterService);
        this.objectName = objectName;
    }

    @Override
    public String getName() {
        return this.objectName;
    }

    @Override
    public String getServiceName() {
        return DistributionTournamentService.NAME;
    }

    @Override
    public String name() {
        return DistributionTournamentService.NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext) {

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    public Tournament.Instance onEnterTournament(String serviceName,String tournamentId,String instanceId,String systemId){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentJoinOperation operation = new TournamentJoinOperation(serviceName,tournamentId,instanceId,systemId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(instanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        final Future<Tournament.Instance> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }

    public Tournament.Entry onScoreTournament(String serviceName,String tournamentId,String instanceId,String systemId,double delta){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentScoreOperation operation = new TournamentScoreOperation(serviceName,tournamentId,instanceId,systemId,delta);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(instanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        final Future<Tournament.Entry> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }



    public Tournament.RaceBoard onListTournament(String serviceName,String tournamentId,String instanceId){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentListOperation operation = new TournamentListOperation(serviceName,tournamentId,instanceId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(instanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        final Future<Tournament.RaceBoard> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }

    public void onFinishTournament(String serviceName,String tournamentId,String instanceId,String systemId){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentFinishOperation operation = new TournamentFinishOperation(serviceName,tournamentId,instanceId,systemId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(instanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        final Future<Tournament.Entry> future = builder.invoke();
        try {
            future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            //return null;
        }
    }

    public void onSyncTournament(String serviceName,String tournamentId,String instanceId){
        NodeEngine nodeEngine = getNodeEngine();
        SyncTournamentOperation operation = new SyncTournamentOperation(serviceName,tournamentId,instanceId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(instanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        final Future<Tournament.Instance> future = builder.invoke();
        try {
            future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            //return null;
        }
    }

    public void onCloseTournament(String serviceName,String tournamentId){
        NodeEngine nodeEngine = getNodeEngine();
        CloseTournamentOperation operation = new CloseTournamentOperation(serviceName,tournamentId);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        mlist.forEach(m->{
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
            } catch (Exception e) {
                future.cancel(true);
                //return false;
            }
        });
    }

    public void onEndTournament(String serviceName,String tournamentId){
        NodeEngine nodeEngine = getNodeEngine();
        EndTournamentOperation operation = new EndTournamentOperation(serviceName,tournamentId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(tournamentId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        final Future<Void> future = builder.invoke();
        try {
            future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
        }
    }
}
