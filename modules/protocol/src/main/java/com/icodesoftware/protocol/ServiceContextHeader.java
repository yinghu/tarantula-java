package com.icodesoftware.protocol;

import com.icodesoftware.*;
import com.icodesoftware.service.*;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;

public class ServiceContextHeader implements ServiceContext {
    @Override
    public DataStore dataStore(int scope, String name) {
        return null;
    }

    @Override
    public DataStore dataStore(ApplicationSchema applicationSchema, int scope, String name) {
        return null;
    }

    @Override
    public Recoverable.DataBufferPair dataBufferPair() {
        return null;
    }

    @Override
    public EventService eventService() {
        return null;
    }

    @Override
    public ClusterProvider clusterProvider() {
        return null;
    }

    @Override
    public ServiceProvider serviceProvider(String name) {
        return null;
    }

    @Override
    public DeploymentServiceProvider deploymentServiceProvider() {
        return null;
    }

    @Override
    public HttpClientProvider httpClientProvider() {
        return null;
    }

    @Override
    public OnPartition[] partitions() {
        return new OnPartition[0];
    }

    @Override
    public OnPartition[] buckets() {
        return new OnPartition[0];
    }

    @Override
    public ClusterProvider.Node node() {
        return null;
    }

    @Override
    public long distributionId() {
        return 0;
    }

    @Override
    public <T extends Recoverable> RecoverableRegistry<T> recoverableRegistry(int registryId) {
        return null;
    }

    @Override
    public void recoverableRegistry(RecoverableListener recoverableListener) {

    }

    @Override
    public Configuration configuration(String config) {
        return null;
    }

    @Override
    public List<Descriptor> availableServices() {
        return List.of();
    }

    @Override
    public void registerAuthVendor(TokenValidatorProvider.AuthVendor authVendor) {

    }

    @Override
    public void unregisterAuthVendor(TokenValidatorProvider.AuthVendor authVendor) {

    }

    @Override
    public Metrics metrics(String name) {
        return null;
    }

    @Override
    public List<String> metricsList() {
        return List.of();
    }

    @Override
    public void registerMetrics(Metrics metrics) {

    }

    @Override
    public void unregisterMetrics(Metrics metrics) {

    }

    @Override
    public PostOffice postOffice() {
        return null;
    }

    @Override
    public Transaction transaction() {
        return null;
    }

    @Override
    public Transaction transaction(int scope) {
        return null;
    }

    @Override
    public ScheduledFuture<?> schedule(SchedulingTask task) {
        return null;
    }

    @Override
    public void log(String message, int level) {

    }

    @Override
    public void log(String message, Exception error, int level) {

    }

    public void execute(Runnable runnable){}

    public <T extends Object> T execute(Callable<T> callable){
        return null;
    }

    public Transaction.LogManager logManager(){
        return null;
    }

    @Override
    public DataStore dataStore(String name) {
        return null;
    }

    public TokenValidator validator(){
        return null;
    }
    public void registerTimerListener(TimerListener timerListener){

    }
    
}
