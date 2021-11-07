package com.icodesoftware.integration.udp;

import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.service.EndPoint;

public class ReplicationEndpoint implements EndPoint {

    private final UDPEndpointServiceProvider udpEndpointServiceProvider;
    private boolean daemon;

    public ReplicationEndpoint(UDPEndpointServiceProvider udpEndpointServiceProvider,boolean daemon){
        this.udpEndpointServiceProvider = udpEndpointServiceProvider;
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
