package com.tarantula.platform.service;

import com.icodesoftware.*;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.GameCluster;

import java.util.List;


public interface ApplicationPreSetup {
    String SET_UP_TYPE = "pre-setup";
    String SET_UP_NAME = "application-pre-setup";
    void setup(ServiceContext serviceContext,Descriptor application,String configName);
    <T extends Configurable> T load(ApplicationContext context,Descriptor application);
    <T extends Configurable> T load(ServiceContext context,Descriptor application);

    <T extends Configurable> void save(ApplicationContext context,Descriptor application,T t);
    <T extends Configurable> boolean load(ApplicationContext context,Descriptor application,T t);
    <T extends Configurable> List<T> list(ApplicationContext context, Descriptor application, RecoverableFactory<T> recoverableFactory);

    <T extends Configurable> void save(ApplicationContext context, GameCluster application, T t);
    <T extends Configurable> List<T> list(ApplicationContext context, GameCluster application, RecoverableFactory<T> recoverableFactory);

}
