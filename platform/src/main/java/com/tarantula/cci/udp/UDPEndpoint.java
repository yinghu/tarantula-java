package com.tarantula.cci.udp;

import com.icodesoftware.Channel;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.UDPEndpointService;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.protocol.UserChannel;
import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.ServiceContext;


public class UDPEndpoint implements EndPoint , UDPEndpointServiceProvider.SessionListener {

    private static final JDKLogger log = JDKLogger.getLogger(UDPEndpoint.class);

    private UDPEndpointServiceProvider udpEndpointServiceProvider;

    public UDPEndpoint(){
        udpEndpointServiceProvider = new UDPEndpointService();
    }
    public void setup(ServiceContext serviceContext){
        udpEndpointServiceProvider.daemon(true);
        udpEndpointServiceProvider.registerUserChannel(new UserChannel(1,udpEndpointServiceProvider,(h, m)->true,this));
        log.warn("UDP Endpoint running as a daemon!");
    }

    @Override
    public void start() throws Exception {
        udpEndpointServiceProvider.start();
    }

    @Override
    public void shutdown() throws Exception {
        udpEndpointServiceProvider.shutdown();
    }

    @Override
    public String name() {
        return EndPoint.UDP_ENDPOINT;
    }

    @Override
    public void address(String address) {
        this.udpEndpointServiceProvider.address(address);
    }

    @Override
    public void backlog(int backlog) {
        this.udpEndpointServiceProvider.backlog(backlog);
    }

    @Override
    public void port(int port) {
        this.udpEndpointServiceProvider.port(port);
    }

    @Override
    public void inboundThreadPoolSetting(String poolSetting) {
        this.udpEndpointServiceProvider.inboundThreadPoolSetting(poolSetting);
    }

    @Override
    public void resource(Resource resource) {

    }

    public Channel register(String systemId){
        return new UDPChannel();
    }

    @Override
    public void onTimeout(int channelId, int sessionId) {
        log.warn("Session->["+sessionId+"] timed out from channel ["+channelId+"]");
    }
}
