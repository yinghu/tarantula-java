package com.tarantula.test;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.GamePortableRegistry;
import com.tarantula.platform.ApplicationConfiguration;
import com.tarantula.platform.item.ItemPortableRegistry;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.service.cluster.PortableRegistry;
import com.tarantula.platform.service.metrics.StatisticsPortableRegistry;
import com.tarantula.platform.service.persistence.ClusterNode;
import com.tarantula.platform.tournament.TournamentPortableRegistry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class TestServiceContext implements ServiceContext {

    ClusterNode node;
    DataStoreProvider dataStoreProvider;

    DataStoreProvider.DistributionIdGenerator distributionIdGenerator;
    public TestServiceContext(DataStoreProvider.DistributionIdGenerator distributionIdGenerator){
        this.distributionIdGenerator = distributionIdGenerator;
        this.node = new ClusterNode("BSD","T01",271,31);
        this.node.clusterNameSuffix("test");
        this.node.nodeId(100);
        this.node.bucketId(200);
        //this.node.deployDirectory = "deploy";
        this.node.servicePushAddress("127.0.0.1");
        this.node.dailyBackupEnabled(false);
        this.node.dataStoreDirectory("target/tld");
        this.node.deployDirectory("target/deploy");
        PresencePortableRegistry registry = new PresencePortableRegistry<>();
        GamePortableRegistry gamePortableRegistry = new GamePortableRegistry();
        StatisticsPortableRegistry statisticsPortableRegistry = new StatisticsPortableRegistry();
        ItemPortableRegistry itemPortableRegistry = new ItemPortableRegistry();
        TournamentPortableRegistry tournamentPortableRegistry = new TournamentPortableRegistry();
    }
    @Override
    public DataStore dataStore(int scope, String s) {
        if(dataStoreProvider==null) return new EmptyDataStore();
        if(scope == Distributable.DATA_SCOPE){
            return dataStoreProvider.createDataStore(s);
        }
        if(scope == Distributable.INTEGRATION_SCOPE){
            return dataStoreProvider.createAccessIndexDataStore(s);
        }
        if(scope== Distributable.INDEX_SCOPE){
            return dataStoreProvider.createKeyIndexDataStore(s);
        }
        if(scope== Distributable.LOG_SCOPE){
            return dataStoreProvider.createLogDataStore(s);
        }
        return new EmptyDataStore();
    }

    @Override
    public ScheduledFuture<?> schedule(SchedulingTask schedulingTask) {
        return null;
    }

    @Override
    public void log(String message, int level) {

    }

    @Override
    public void log(String message, Exception error, int level) {

    }

    @Override
    public void recoverableRegistry(RecoverableListener recoverableListener) {

    }

    @Override
    public EventService eventService() {
        return null;
    }

    @Override
    public ClusterProvider clusterProvider() {
        return new TestClusterProvider();
    }

    @Override
    public ServiceProvider serviceProvider(String s) {
        return null;
    }

    @Override
    public DeploymentServiceProvider deploymentServiceProvider() {
        return new TestDeploymentProvider(dataStoreProvider);
    }

    @Override
    public long distributionId() {
        return distributionIdGenerator.id();
    }

    @Override
    public OnPartition[] partitions() {
        return new OnPartition[0];
    }

    public OnPartition[] buckets(){
        return new OnPartition[0];
    }


    @Override
    public <T extends Recoverable> RecoverableRegistry<T> recoverableRegistry(int i) {
        return new PortableRegistry<T>();
    }
    public List<String> metricsList(){
        return null;
    }
    @Override
    public Configuration configuration(String conf) {
        try{
            Map<String,Object> kv = JsonUtil.toMap(Thread.currentThread().getContextClassLoader().getResourceAsStream(conf+".json"));
            ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();
            kv.forEach((k,v)->applicationConfiguration.property(k,v));
            return applicationConfiguration;
        }catch (Exception ex){
            return new ApplicationConfiguration();
        }
    }

    @Override
    public List<Descriptor> availableServices() {
        return null;
    }

    @Override
    public void registerAuthVendor(TokenValidatorProvider.AuthVendor authVendor) {

    }

    @Override
    public void unregisterAuthVendor(TokenValidatorProvider.AuthVendor authVendor) {

    }

    @Override
    public Metrics metrics(String s) {
        return null;
    }

    @Override
    public void registerMetrics(Metrics metrics) {

    }

    @Override
    public void unregisterMetrics(Metrics metrics) {

    }



    public ClusterProvider.Node node(){
        return node;
    }
    public HttpClientProvider httpClientProvider(){
        return null;
    }
    public PostOffice postOffice(){
        return null;
    }

    public Transaction transaction(int scope){
        return dataStoreProvider.transaction(scope);
    }

    public Recoverable.DataBufferPair dataBufferPair(){
        return null;
    }
    public DataStore dataStore(ApplicationSchema applicationSchema,int scope,String name){
        return null;
    }
}
