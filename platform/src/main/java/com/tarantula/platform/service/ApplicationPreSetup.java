package com.tarantula.platform.service;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.GameCluster;

import java.util.List;

public interface ApplicationPreSetup {

    String SET_UP_NAME = "application-pre-setup";
    //[game]_service
    void setup(ServiceContext serviceContext,Descriptor application,String configName);

    <T extends Configurable> T load(Descriptor application);


    <T extends Configurable> boolean save(Descriptor application,T t);
    <T extends Configurable> boolean load(Descriptor application,T t);

    <T extends Configurable> List<T> list(Descriptor application, RecoverableFactory<T> recoverableFactory);


    //[game]_service_configuration data store
    <T extends Configurable> boolean save(GameCluster gameCluster, T t);

    <T extends Configurable> boolean load(GameCluster gameCluster, T t);

    <T extends Configurable> List<T> list(GameCluster gameCluster, RecoverableFactory<T> recoverableFactory);

    DataStore dataStore(GameCluster gameCluster);
    DataStore dataStore(GameCluster gameCluster,String service);

    DataStore dataStore(Descriptor descriptor);

    void setup(ServiceContext serviceContext);
}
