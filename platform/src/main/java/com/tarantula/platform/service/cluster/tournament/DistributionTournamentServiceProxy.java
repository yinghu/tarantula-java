package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.Tournament;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.ClusterUtil;
import com.tarantula.platform.tournament.DistributionTournamentService;
import com.tarantula.platform.tournament.TournamentOnNode;
import com.tarantula.platform.tournament.TournamentRegisterStatus;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DistributionTournamentServiceProxy extends AbstractDistributedObject<TournamentClusterService> implements DistributionTournamentService {

    private String objectName;
    private MetricsListener metricsListener;
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

    public boolean ownership(long tournamentId){
        NodeEngine nodeEngine = getNodeEngine();
        int pid = nodeEngine.getPartitionService().getPartitionId(tournamentId);
        return nodeEngine.getPartitionService().isPartitionOwner(pid);
    }
    public int partitionId(long tournamentId){
        NodeEngine nodeEngine = getNodeEngine();
        return nodeEngine.getPartitionService().getPartitionId(tournamentId);
    }
    public TournamentRegisterStatus onRegisterTournament(String serviceName, long tournamentId, int slot){
        NodeEngine nodeEngine = getNodeEngine();
        RegisterTournamentOperation operation = new RegisterTournamentOperation(serviceName,tournamentId,slot);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(tournamentId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<TournamentRegisterStatus> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
        return (TournamentRegisterStatus)result.result;

    }

    public long onEnterGlobalTournament(String serviceName,long tournamentId,long segmentInstanceId,long systemId){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentSegmentJoinOperation operation = new TournamentSegmentJoinOperation(serviceName,tournamentId,segmentInstanceId,systemId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(segmentInstanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Long> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
        return (long)result.result;

    }

    public double onScoreGlobalTournament(String serviceName,long tournamentId,long instanceId,long entryId,long systemId,double credit,double delta){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentScoreSegmentOperation operation = new TournamentScoreSegmentOperation(serviceName,tournamentId,instanceId,entryId,systemId,credit,delta);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(instanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Boolean> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
        return (double)result.result;

    }

    public Tournament.Instance onEnterTournament(String serviceName,long tournamentId,long instanceId,long systemId){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentJoinOperation operation = new TournamentJoinOperation(serviceName,tournamentId,instanceId,systemId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(instanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Tournament.Instance> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
        return (Tournament.Instance)result.result;

    }

    public double onScoreTournament(String serviceName,long tournamentId,long instanceId,long systemId,double credit,double delta){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentScoreOperation operation = new TournamentScoreOperation(serviceName,tournamentId,instanceId,systemId,credit,delta);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(instanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Boolean> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
        return (double)result.result;

    }


    public byte[] onRaceBoard(String serviceName,long tournamentId,long instanceId){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentRaceBoardOperation operation = new TournamentRaceBoardOperation(serviceName,tournamentId,instanceId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(instanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<byte[]> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
        return (byte[])result.result;
    }

    public byte[] onMyRaceBoard(String serviceName,long tournamentId,long instanceId,long entryId,long systemId){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentMyRaceBoardOperation operation = new TournamentMyRaceBoardOperation(serviceName,tournamentId,instanceId,entryId,systemId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(instanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<byte[]> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
        return (byte[])result.result;
    }



    public void onEndTournament(String serviceName,long tournamentId){
        NodeEngine nodeEngine = getNodeEngine();
        EndTournamentOperation operation = new EndTournamentOperation(serviceName,tournamentId);
        Set<Member> members = nodeEngine.getClusterService().getMembers();
        for(Member member : members){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,member.getAddress());
            ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<Void> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            if(!result.successful) throw new RuntimeException(result.exception);
        }
    }

    public List<TournamentOnNode> onScan(String serviceName){
        NodeEngine nodeEngine = getNodeEngine();
        ScanTournamentOperation operation = new ScanTournamentOperation(serviceName);
        Set<Member> members = nodeEngine.getClusterService().getMembers();
        List<TournamentOnNode> tournamentOnNodes = new ArrayList<>();
        for(Member member : members){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,member.getAddress());
            ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<byte[]> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            if(!result.successful) throw new RuntimeException(result.exception);
            byte[] ret = (byte[])result.result;
            ByteBuffer byteBuffer = ByteBuffer.wrap(ret);
            TournamentOnNode tournamentOnNode = new TournamentOnNode();
            tournamentOnNode.nodeId = member.getAddress().getHost();
            int sz = byteBuffer.getInt();
            tournamentOnNode.onScheduled = new long[sz];
            for(int i=0;i<sz;i++){
                tournamentOnNode.onScheduled[i]=byteBuffer.getLong();
            }
            tournamentOnNodes.add(tournamentOnNode);
        }
        return tournamentOnNodes;
    }

    public void registerMetricsListener(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }
    public void releaseMetricsListener(){
        this.metricsListener = (k,v)->{};
    }
}
