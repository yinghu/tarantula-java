package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.Tournament;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.tournament.DistributionTournamentService;

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

    public  boolean checkAvailable(String serviceName,String tournamentId){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentCheckAvailableOperation operation = new TournamentCheckAvailableOperation(serviceName,tournamentId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(tournamentId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        final Future<Boolean> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return false;
        }
    }
    public String join(String serviceName, String tournamentId, String systemId){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentRegisterOperation operation = new TournamentRegisterOperation(serviceName,tournamentId,systemId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(tournamentId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        final Future<String> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }
    public byte[] enter(String serviceName,String tournamentId,String instanceId,String systemId){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentJoinOperation operation = new TournamentJoinOperation(serviceName,tournamentId,instanceId,systemId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(instanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        final Future<byte[]> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }
    public byte[] score(String serviceName,String instanceId,String systemId,double delta){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentScoreOperation operation = new TournamentScoreOperation(serviceName,instanceId,systemId,delta);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(instanceId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,partitionId);
        final Future<byte[]> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }
    public byte[] schedule(String serviceName, Tournament.Schedule schedule){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentScheduleOperation operation = new TournamentScheduleOperation(serviceName,schedule);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionTournamentService.NAME,operation,nodeEngine.getMasterAddress());
        final Future<byte[]> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }
    public boolean localPartition(String distributionKey){
        NodeEngine nodeEngine = getNodeEngine();
        int pid = nodeEngine.getPartitionService().getPartitionId(distributionKey);
        return nodeEngine.getPartitionService().getPartition(pid).isLocal();
    }
}
