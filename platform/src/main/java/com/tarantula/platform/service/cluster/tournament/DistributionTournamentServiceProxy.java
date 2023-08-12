package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.Tournament;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.ClusterUtil;
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
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Tournament.Instance> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        if(!result.successful) throw new RuntimeException(result.exception);
        return (Tournament.Instance)result.result;

    }

    public Tournament.Entry onScoreTournament(String serviceName,String tournamentId,String instanceId,String systemId,double credit,double delta){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentScoreOperation operation = new TournamentScoreOperation(serviceName,tournamentId,instanceId,systemId,credit,delta);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(instanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Tournament.Entry> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        if(!result.successful) throw new RuntimeException(result.exception);
        return (Tournament.Entry)result.result;

    }


    public Tournament.RaceBoard onListTournament(String serviceName,String tournamentId,String instanceId){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentListOperation operation = new TournamentListOperation(serviceName,tournamentId,instanceId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(instanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Tournament.RaceBoard> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        if(!result.successful) throw new RuntimeException(result.exception);
        return (Tournament.RaceBoard)result.result;
    }

    public void onFinishTournament(String serviceName,String tournamentId,String instanceId,String systemId){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentFinishOperation operation = new TournamentFinishOperation(serviceName,tournamentId,instanceId,systemId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(instanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Tournament.Entry> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        if(!result.successful) throw new RuntimeException(result.exception);
    }

    public void onSyncTournament(String serviceName,String tournamentId,String instanceId){
        NodeEngine nodeEngine = getNodeEngine();
        SyncTournamentOperation operation = new SyncTournamentOperation(serviceName,tournamentId,instanceId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(instanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Tournament.Instance> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        if(!result.successful) throw new RuntimeException(result.exception);

    }

    public void onCloseTournament(String serviceName,String tournamentId){
        NodeEngine nodeEngine = getNodeEngine();
        CloseTournamentOperation operation = new CloseTournamentOperation(serviceName,tournamentId);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        mlist.forEach(m->{
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<Void> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            });
            if(!result.successful) throw new RuntimeException(result.exception);
        });
    }

    public void onEndTournament(String serviceName,String tournamentId){
        NodeEngine nodeEngine = getNodeEngine();
        EndTournamentOperation operation = new EndTournamentOperation(serviceName,tournamentId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(tournamentId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Void> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        if(!result.successful) throw new RuntimeException(result.exception);
    }
}
