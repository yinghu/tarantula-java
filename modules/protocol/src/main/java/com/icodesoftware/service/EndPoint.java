package com.icodesoftware.service;


import com.icodesoftware.Channel;

public interface EndPoint extends ServiceProvider {

    String HTTP_ENDPOINT = "HTTPEndpoint";
    String UDP_ENDPOINT = "UDPEndpoint";

    void address(String address);
    void backlog(int backlog);
    void port(int port);
    void inboundThreadPoolSetting(String inboundThreadPoolSetting);
    void resource(Resource resource);
    default Channel register(String systemId){return  null;}

    interface Resource{
        RequestHandler requestHandler(String name);
    }
}
