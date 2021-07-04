package com.icodesoftware.service;

public interface ServiceProvider extends Serviceable {

    String name();

    default void setup(ServiceContext serviceContext){}
    default void waitForData(){}
    default void registerMetricsListener(MetricsListener metricsListener){}
    default void unregisterListener(String registerKey){}
    //midnight check
    default void atMidnight(){}
}
