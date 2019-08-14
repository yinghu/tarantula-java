package com.tarantula;

/**
 * Updated by yinghu lu on 4/17/2018.
 * ServiceProvider provides the API to hook the business logic to application context
 */
public interface ServiceProvider extends Serviceable{

    String name();

    void setup(ServiceContext serviceContext);

    void waitForData();

}
