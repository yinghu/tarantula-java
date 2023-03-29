package com.tarantula.test;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.tarantula.platform.ApplicationConfiguration;
import com.tarantula.platform.service.persistence.ClusterNode;
import com.tarantula.platform.util.SystemUtil;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class TestServiceContext implements ServiceContext {

    ClusterNode node;


    public TestServiceContext(){
        this.node = new ClusterNode("BSD","TS01",31);
        this.node.clusterNameSuffix = "test";
        this.node.deployDirectory = "deploy";
        this.node.servicePushAddress = "127.0.0.1";
        this.node.runAsMirror = false;
        this.node.backupEnabled = false;
        this.node.dailyBackupEnabled = false;
        this.node.dataStoreDirectory = "target/tld";
        node.bucketId = node.bucketName()+"/"+ SystemUtil.oid();
        node.nodeId =node.bucketName()+"/"+ SystemUtil.oid();

    }
    @Override
    public DataStore dataStore(String s, int i) {
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
        return null;
    }

    @Override
    public AccessIndexService accessIndexService() {
        return null;
    }

    @Override
    public TarantulaLogger logger(Class aClass) {

        return new EmptyLogger();
    }

    @Override
    public OnPartition[] partitions() {
        return new OnPartition[0];
    }




    @Override
    public RecoverableRegistry recoverableRegistry(int i) {
        return null;
    }

    @Override
    public TokenValidatorProvider.AuthVendor authVendor(String s) {
        return null;
    }

    @Override
    public Configuration configuration(String s) {
        return new ApplicationConfiguration();
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

    public void registerBackupProvider(BackupProvider backupProvider){}
    public void unregisterBackupProvider(BackupProvider backupProvider){}

    public BackupProvider backupProvider(){
        return null;
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
}
