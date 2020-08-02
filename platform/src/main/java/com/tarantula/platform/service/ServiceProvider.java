package com.tarantula.platform.service;

/**
 * Updated by yinghu lu on 5/2/2020
 * ServiceProvider provides the API to hook the business logic to application context
 */
public interface ServiceProvider extends Serviceable {

    String name();

    void setup(ServiceContext serviceContext);

    default void waitForData(){}
    default void registerMetricsListener(MetricsListener metricsListener){}

    //midnight check
    default void atMidnight(){}
}
