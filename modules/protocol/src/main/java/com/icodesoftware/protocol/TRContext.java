package com.icodesoftware.protocol;

import com.icodesoftware.*;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.DataStoreProvider;

import java.util.concurrent.*;

public class TRContext implements Context {

    protected TarantulaLogger tarantulaLogger;
    protected ScheduledExecutorService scheduledExecutorService;
    protected ExecutorService executorService;
    protected DataStoreProvider dataStoreProvider;
    protected Transaction.LogManager logManager;
    protected DataStoreProvider.DistributionIdGenerator distributionIdGenerator;
    protected TokenValidator tokenValidator;

    protected final CopyOnWriteArrayList<TimerListener> timerListeners = new CopyOnWriteArrayList<>();

    @Override
    public ScheduledFuture<?> schedule(SchedulingTask task) {
        if(task.oneTime()){
            return this.scheduledExecutorService.schedule(task,task.initialDelay()+task.delay(), TimeUnit.MILLISECONDS);
        }else{
            return this.scheduledExecutorService.scheduleAtFixedRate(task,task.initialDelay(),task.delay(),TimeUnit.MILLISECONDS);
        }
    }

    public void log(String message,int level){
        switch (level){
            case OnLog.DEBUG:
                this.tarantulaLogger.debug(message);
                break;
            case OnLog.INFO:
                this.tarantulaLogger.info(message);
                break;
            case OnLog.WARN:
                this.tarantulaLogger.warn(message);
                break;
        }

    }
    public void log(String message,Exception error,int level){
        switch (level){
            case OnLog.WARN:
                if(error!=null){
                    this.tarantulaLogger.warn(message);
                }
                else{
                    this.tarantulaLogger.warn(message,error);
                }
                break;
            case OnLog.ERROR:
                this.tarantulaLogger.error(message,error);
                break;
        }
    }

    @Override
    public void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

    @Override
    public <T> T execute(Callable<T> callable) {
        try{
            return executorService.submit(callable).get(10,TimeUnit.SECONDS);
        }catch (Exception ex){
            throw new RuntimeException("callable timeout");
        }
    }

    @Override
    public PostOffice postOffice() {
        return null;
    }

    @Override
    public Transaction transaction() {
        return transaction(Distributable.DATA_SCOPE);
    }

    @Override
    public Transaction transaction(int scope) {
        return dataStoreProvider.transaction(scope);
    }

    @Override
    public Transaction.LogManager logManager() {
        return logManager;
    }

    @Override
    public DataStore dataStore(String name) {
        return dataStore(Distributable.DATA_SCOPE,name);
    }

    @Override
    public DataStore dataStore(int scope, String name) {
        switch (scope){
            case Distributable.DATA_SCOPE -> {
                return dataStoreProvider.createDataStore(name);
            }
            case Distributable.INTEGRATION_SCOPE -> {
                return dataStoreProvider.createAccessIndexDataStore(name);
            }
            case Distributable.INDEX_SCOPE -> {
                return dataStoreProvider.createKeyIndexDataStore(name);
            }
            case Distributable.LOG_SCOPE -> {
                return dataStoreProvider.createLogDataStore(name);
            }
            case Distributable.LOCAL_SCOPE -> {
                return dataStoreProvider.createLocalDataStore(name);
            }
            default -> throw new UnsupportedOperationException("scope ["+scope+"] not supported");
        }
    }

    @Override
    public long distributionId() {
        return distributionIdGenerator.id();
    }

    @Override
    public ClusterProvider.Node node() {
        return null;
    }

    @Override
    public TokenValidator validator() {
        return tokenValidator;
    }

    @Override
    public void registerTimerListener(TimerListener timerListener) {
        if(timerListener==null) return;
        timerListeners.add(timerListener);
    }
}
