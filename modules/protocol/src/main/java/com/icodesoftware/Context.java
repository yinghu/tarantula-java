package com.icodesoftware;

import java.util.concurrent.ScheduledFuture;

public interface Context {

    ScheduledFuture<?> schedule(SchedulingTask task);

    void log(String message,int level);

    void log(String message,Exception error,int level);

    default void execute(Runnable runnable){}

    default PostOffice postOffice(){throw new UnsupportedOperationException(); }
    default PostOffice postOffice(Session session){ throw new UnsupportedOperationException();}
}
