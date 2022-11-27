package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

public interface ServiceProvider extends Serviceable {

    String name();
    //start -> setup -> waitForData -> shutdown
    default void setup(ServiceContext serviceContext){}
    default void waitForData(){}
    default void registerMetricsListener(MetricsListener metricsListener){}
    default void releaseMetricsListener(){}

    //midnight check
    default void atMidnight(){}

    default void registerSummary(Summary summary){}
    default void updateSummary(Summary summary){}

    interface Summary extends Recoverable{
        default void update(String category,int value){}
        default void update(String category,long value){}
        default void update(String category,double value){}
        default void registerCategory(String category){}
    }
}
