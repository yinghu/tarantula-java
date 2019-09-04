package com.tarantula.platform.service;

import com.tarantula.cci.RequestHandler;

/**
 * Updated by yinghu lu on 6/15/2019.
 */
public interface EndPoint extends ServiceProvider {

    long CHECK_POINT_DELTA = 60000;

    void address(String address);
    void backlog(int backlog);
    void port(int port);
    void secured(boolean secured);
    void password(String password);
    void inboundThreadPoolSetting(String inboundThreadPoolSetting);

    void resource(Resource resource);

    interface Resource{
        RequestHandler requestHandler(String name);
    }
}
