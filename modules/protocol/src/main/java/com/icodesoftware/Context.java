package com.icodesoftware;

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
}
