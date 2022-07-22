package com.tarantula.platform.service.cluster.tournament;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.Tournament;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.tournament.DistributionTournamentService;
import com.tarantula.platform.tournament.TournamentHeaderIndex;

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

    public String register(String serviceName, String tournamentId, String systemId){
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

    public Tournament.Instance join(String serviceName,String tournamentId,String instanceId,String systemId){
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

    public Tournament.Entry score(String serviceName,String instanceId,String systemId,double delta){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentScoreOperation operation = new TournamentScoreOperation(serviceName,instanceId,systemId,delta);
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

    public Tournament.Entry configure(String serviceName,String instanceId,String systemId,byte[] payload){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentConfigureOperation operation = new TournamentConfigureOperation(serviceName,instanceId,systemId,payload);
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

    public Tournament.RaceBoard list(String serviceName,String instanceId){
        NodeEngine nodeEngine = getNodeEngine();
        TournamentListOperation operation = new TournamentListOperation(serviceName,instanceId);
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

    public TournamentHeaderIndex localManaged(String key){
        int pid = getNodeEngine().getPartitionService().getPartitionId(key);
        return new TournamentHeaderIndex(pid,getNodeEngine().getPartitionService().getPartition(pid).isLocal());
    }


    public void closeTournament(String serviceName,String tournamentId){
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

    public void endTournament(String serviceName,String tournamentId){
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
