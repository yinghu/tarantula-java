package com.tarantula.platform.service;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.service.ServiceContext;

import java.util.List;


public interface ApplicationPreSetup {
    String SET_UP_TYPE = "pre-setup";
    String SET_UP_NAME = "application-pre-setup";
    void setup(ServiceContext serviceContext,Descriptor application,String configName);
    <T extends Recoverable> T load(ApplicationContext context,Descriptor application);
    <T extends Recoverable> T load(ServiceContext context,Descriptor application);

    <T extends Recoverable> void save(ApplicationContext context,Descriptor application,T t);
    <T extends Recoverable> boolean load(ApplicationContext context,Descriptor application,T t);
    <T extends Recoverable> List<T> list(ApplicationContext context, Descriptor application, RecoverableFactory<T> recoverableFactory);

}
