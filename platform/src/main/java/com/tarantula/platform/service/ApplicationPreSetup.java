package com.tarantula.platform.service;

import com.icodesoftware.ApplicationContext;
import com.icodesoftware.Descriptor;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.ServiceContext;


public interface ApplicationPreSetup {
    String SET_UP_TYPE = "pre-setup";
    String SET_UP_NAME = "application-pre-setup";
    void setup(ServiceContext serviceContext,Descriptor application);
    <T extends Recoverable> T load(ApplicationContext context,Descriptor application);
    <T extends Recoverable> T load(ServiceContext context,Descriptor application);
}
