package com.icodesoftware;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;

public interface Context {

    ScheduledFuture<?> schedule(SchedulingTask task);

    void log(String message,int level);

    void log(String message,Exception error,int level);

    default void execute(Runnable runnable){}
    default <T extends Object> T execute(Callable<T> callable){ return null;}

}
