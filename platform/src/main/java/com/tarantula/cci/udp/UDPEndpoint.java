package com.tarantula.cci.udp;

import com.icodesoftware.Channel;
import com.icodesoftware.Connection;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.UDPEndpointService;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.protocol.UserChannel;
import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.UniverseConnection;


public class UDPEndpoint implements EndPoint , UDPEndpointServiceProvider.SessionListener,UDPEndpointServiceProvider.UserSessionValidator,UDPEndpointServiceProvider.RequestListener {

    private static final JDKLogger log = JDKLogger.getLogger(UDPEndpoint.class);

    private UDPEndpointServiceProvider udpEndpointServiceProvider;
    private int channelId;
    private int sessionId;
    private Connection connection;

    public UDPEndpoint(){
        connection = new UniverseConnection();
        udpEndpointServiceProvider = new UDPEndpointService();
        channelId = 1;
        sessionId = 1;
    }
    public void setup(ServiceContext serviceContext){
        connection.serverId("serverId");
        connection.type("udp");
        connection.secured(true);
        connection.host("10.0.0.192");
        connection.port(11933);
        udpEndpointServiceProvider.daemon(true);
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
        UserChannel userChannel = new UserChannel(channelId++,udpEndpointServiceProvider,this,this,this);
        udpEndpointServiceProvider.registerUserChannel(userChannel);
        return new UDPChannel(this.connection,userChannel,sessionId++);
    }

    @Override
    public void onTimeout(int channelId, int sessionId) {
        log.warn("Session->["+sessionId+"] timed out from channel ["+channelId+"]");
    }

    @Override
    public boolean validate(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        return true;
    }

    @Override
    public void onMessage(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        log.warn(messageHeader.toString()+">>"+messageHeader.commandId);
        log.warn("Payload->"+messageBuffer.readUTF8());
    }
}
