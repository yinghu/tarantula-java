package com.tarantula.platform.service;

import com.icodesoftware.service.ServiceProvider;
import com.tarantula.cci.RequestHandler;


public interface EndPoint extends ServiceProvider {

    //long CHECK_POINT_DELTA = 60000;

    void address(String address);
    void backlog(int backlog);
    void port(int port);
    void secured(boolean secured);
    void inboundThreadPoolSetting(String inboundThreadPoolSetting);

    void resource(Resource resource);

    interface Resource{
        RequestHandler requestHandler(String name);
    }
}
