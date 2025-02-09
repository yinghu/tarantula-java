package com.icodesoftware.service;


import com.icodesoftware.protocol.Channel;
import com.icodesoftware.Session;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;

import java.util.concurrent.Executor;

public interface EndPoint extends ServiceProvider {

    String HTTP_ENDPOINT = "HTTPEndpoint";
    String UDP_ENDPOINT = "UDPEndpoint";
    String TCP_ENDPOINT = "TCPEndpoint";

    void address(String address);
    default void backlog(int backlog){}
    void port(int port);
    int port();
    default void inboundThreadPoolSetting(String inboundThreadPoolSetting){}
    default void executor(Executor executor){}
    default void resource(String resource){}
    default void resource(Resource resource){}
    default Channel register(Session session, UDPEndpointServiceProvider.RequestListener requestListener, Session.TimeoutListener timeoutListener){return  null;}

    interface Resource{
        RequestHandler requestHandler(String name);
    }

    interface Listener{
        void onStart(EndPoint endPoint);
        //void onStop(EndPoint endPoint);
    }
}
