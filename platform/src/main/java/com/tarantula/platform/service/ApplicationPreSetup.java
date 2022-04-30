package com.tarantula.platform.service;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.GameCluster;

import java.util.List;

public interface ApplicationPreSetup {

    String SET_UP_NAME = "application-pre-setup";
    //[game]_service
    void setup(ServiceContext serviceContext,Descriptor application,String configName);

    <T extends Configurable> T load(ApplicationContext context,Descriptor application);
    <T extends Configurable> T load(ServiceContext context,Descriptor application);

    <T extends Configurable> boolean save(ApplicationContext context,Descriptor application,T t);
    <T extends Configurable> boolean load(ApplicationContext context,Descriptor application,T t);

    <T extends Configurable> List<T> list(ApplicationContext context, Descriptor application, RecoverableFactory<T> recoverableFactory);

    <T extends Configurable> boolean load(ServiceContext context,Descriptor application,T t);
    <T extends Configurable> List<T> list(ServiceContext context, Descriptor application, RecoverableFactory<T> recoverableFactory);

    //[game]_service_configuration data store
    <T extends Configurable> boolean save(ApplicationContext context, GameCluster gameCluster, T t);
    <T extends Configurable> boolean save(ServiceContext context,GameCluster gameCluster,T t);
    <T extends Configurable> boolean load(ApplicationContext context, GameCluster gameCluster, T t);
    <T extends Configurable> boolean load(ServiceContext context,GameCluster gameCluster,T t);
    <T extends Configurable> List<T> list(ApplicationContext context, GameCluster gameCluster, RecoverableFactory<T> recoverableFactory);

    DataStore dataStore(ServiceContext serviceContext,GameCluster gameCluster,String service);
}
