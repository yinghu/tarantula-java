package com.icodesoftware;

import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.HttpClientProvider;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;

public interface Context {

    ScheduledFuture<?> schedule(SchedulingTask task);

    void log(String message,int level);

    void log(String message,Exception error,int level);

    void execute(Runnable runnable);

    <T extends Object> T execute(Callable<T> callable);

    PostOffice postOffice();

    Transaction transaction();

    Transaction transaction(int scope);

    Transaction.LogManager logManager();

    DataStore dataStore(String name);

    DataStore dataStore(int scope,String name);

    long distributionId();

    ClusterProvider.Node node();

    TokenValidator validator();

    HttpClientProvider httpClientProvider();

    void registerTimerListener(TimerListener timerListener);

    interface TimerListener{
        void atMidnight(LocalDateTime now);
    }
}
