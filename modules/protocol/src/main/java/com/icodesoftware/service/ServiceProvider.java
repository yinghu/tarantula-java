package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

public interface ServiceProvider extends Serviceable {

    String name();

    default void setup(ServiceContext serviceContext){}
    default void waitForData(){}
    default void registerMetricsListener(MetricsListener metricsListener){}
    default void releaseMetricsListener(){}
    //default void unregisterListener(String registerKey){}
    //midnight check
    default void atMidnight(){}

    default void updateSummary(Summary summary){}

    interface Summary extends Recoverable{
        void update(String category,int value);
        void update(String category,long value);
        void update(String category,double value);
    }
}
