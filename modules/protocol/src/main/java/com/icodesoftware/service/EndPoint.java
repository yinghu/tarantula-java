package com.icodesoftware.service;


import com.icodesoftware.Channel;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;

public interface EndPoint extends ServiceProvider {

    String HTTP_ENDPOINT = "HTTPEndpoint";
    String UDP_ENDPOINT = "UDPEndpoint";

    void address(String address);
    default void backlog(int backlog){}
    void port(int port);
    void inboundThreadPoolSetting(String inboundThreadPoolSetting);
    default void resource(Resource resource){}
    default Channel register(String systemId, UDPEndpointServiceProvider.RequestListener requestListener){return  null;}

    interface Resource{
        RequestHandler requestHandler(String name);
    }
}
