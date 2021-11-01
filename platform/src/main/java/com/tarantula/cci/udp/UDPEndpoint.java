package com.tarantula.cci.udp;

import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.UDPEndpointService;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.protocol.UserChannel;
import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.ServiceContext;


public class UDPEndpoint implements EndPoint {

    private static final JDKLogger log = JDKLogger.getLogger(UDPEndpoint.class);

    private UDPEndpointServiceProvider udpEndpointServiceProvider;

    public UDPEndpoint(){
        udpEndpointServiceProvider = new UDPEndpointService();
    }
    public void setup(ServiceContext serviceContext){
        udpEndpointServiceProvider.daemon(true);
        udpEndpointServiceProvider.registerUserChannel(new UserChannel(1,udpEndpointServiceProvider,(h, m)->true));
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
        return "UDPEndpoint";
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
}
