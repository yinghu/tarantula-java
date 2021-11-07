package com.icodesoftware.integration.udp;

import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.service.EndPoint;
import com.icodesoftware.util.HttpCaller;

public class ReplicationEndpoint implements EndPoint {

    private final UDPEndpointServiceProvider udpEndpointServiceProvider;
    private final boolean daemon;
    public final String serverId;
    private HttpCaller httpCaller;

    public ReplicationEndpoint(UDPEndpointServiceProvider udpEndpointServiceProvider,String serverId,boolean daemon){
        this.udpEndpointServiceProvider = udpEndpointServiceProvider;
        this.serverId = serverId;
        this.udpEndpointServiceProvider.daemon(daemon);
        this.daemon = daemon;
    }

    @Override
    public void address(String host) {
        this.udpEndpointServiceProvider.address(host);
    }

    @Override
    public void port(int port) {
        this.udpEndpointServiceProvider.port(port);
    }

    @Override
    public void inboundThreadPoolSetting(String inboundThreadPoolSetting) {
        this.udpEndpointServiceProvider.inboundThreadPoolSetting(inboundThreadPoolSetting);
    }

    @Override
    public String name() {
        return "ReplicationService";
    }

    @Override
    public void start() throws Exception {
        this.udpEndpointServiceProvider.start();
        if(!daemon) this.udpEndpointServiceProvider.run();
    }

    @Override
    public void shutdown() throws Exception {
        this.udpEndpointServiceProvider.shutdown();
    }
}
