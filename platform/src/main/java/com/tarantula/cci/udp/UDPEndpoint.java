package com.tarantula.cci.udp;

import com.icodesoftware.*;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.UDPEndpointService;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.protocol.UserChannel;
import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.CipherUtil;
import com.tarantula.platform.UniverseConnection;

import javax.crypto.Cipher;
import java.util.Base64;
import java.util.UUID;


public class UDPEndpoint implements EndPoint , UDPEndpointServiceProvider.SessionListener,UDPEndpointServiceProvider.UserSessionValidator,UDPEndpointServiceProvider.RequestListener {

    private TarantulaLogger logger;
    private UDPEndpointServiceProvider udpEndpointServiceProvider;
    private ServiceContext serviceContext;
    private TokenValidatorProvider tokenValidator;
    private int channelId;
    private int sessionId;
    private byte[] key;
    private String serverKey;
    private Connection connection;

    public UDPEndpoint(){
        connection = new UniverseConnection();
        udpEndpointServiceProvider = new UDPEndpointService();
        channelId = 1;
        sessionId = 1;
    }
    public void setup(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
        this.tokenValidator = (TokenValidatorProvider) serviceContext.serviceProvider(TokenValidatorProvider.NAME);
        logger = serviceContext.logger(UDPEndpoint.class);
        this.key = serviceContext.deploymentServiceProvider().serverKey();
        this.serverKey = Base64.getEncoder().encodeToString(key);
        connection.serverId(UUID.randomUUID().toString());
        connection.type(Connection.UDP);
        connection.secured(true);
        connection.host("10.0.0.192");
        connection.port(11933);
        udpEndpointServiceProvider.daemon(true);
        logger.warn("UDP Endpoint running as a daemon!");
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
        return new UDPChannel(this.connection,userChannel,sessionId++,serverKey);
    }

    @Override
    public void onTimeout(int channelId, int sessionId) {
        logger.warn("Session->["+sessionId+"] timed out from channel ["+channelId+"]");
    }

    @Override
    public boolean validate(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        int sessionId = messageBuffer.readInt();
        String token = messageBuffer.readUTF8();
        OnSession session = tokenValidator.tokenValidator().validateToken(token);
        logger.warn(session.systemId());
        return sessionId==messageHeader.sessionId;
    }

    @Override
    public void onMessage(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        logger.warn(messageHeader.toString()+">>"+messageHeader.commandId);
        if(messageHeader.encrypted){
            try{
                Cipher cipher = CipherUtil.decrypt(key);
                byte[] plain = cipher.doFinal(messageBuffer.readPayload());
                messageBuffer.reset();
                messageBuffer.writeHeader(messageHeader);
                messageBuffer.writePayload(plain);
                messageBuffer.flip();
                messageBuffer.readHeader();
                String ret = messageBuffer.readUTF8();
                logger.warn(ret);

            }catch (Exception ex){
                logger.error("error",ex);
            }
        }
        else{
            logger.warn(messageBuffer.readUTF8());
        }
    }
}
