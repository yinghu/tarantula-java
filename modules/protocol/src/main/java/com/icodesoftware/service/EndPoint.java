package com.icodesoftware.service;


public interface EndPoint extends ServiceProvider {

    void address(String address);
    void backlog(int backlog);
    void port(int port);
    void inboundThreadPoolSetting(String inboundThreadPoolSetting);
    void resource(Resource resource);

    interface Resource{
        RequestHandler requestHandler(String name);
    }
}
