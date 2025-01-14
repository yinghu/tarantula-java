package com.icodesoftware.protocol;

import com.icodesoftware.service.ServiceContext;
import com.sun.net.httpserver.HttpHandler;

abstract public class AbstractHttpHandler implements HttpHandler {

    protected ServiceContext serviceContext;

    public void setup(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
    }
}
