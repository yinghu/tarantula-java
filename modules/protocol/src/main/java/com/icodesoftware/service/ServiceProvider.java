package com.icodesoftware.service;


/**
 * Updated by yinghu lu on 5/2/2020
 * ServiceProvider provides the API to hook the business logic to application context
 */
public interface ServiceProvider extends Serviceable {

    String name();

    void setup(ServiceContext serviceContext);

    default void waitForData(){}
    default void registerMetricsListener(MetricsListener metricsListener){}

    //default void register(Configurable configurable){}
    //default void updateForData(int factoryId,int classId,String key,byte[] value){}
    //midnight check
    default void atMidnight(){}
}
