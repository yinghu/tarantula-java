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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class UDPEndpoint implements EndPoint , UDPEndpointServiceProvider.SessionListener,UDPEndpointServiceProvider.UserSessionValidator,UDPEndpointServiceProvider.RequestListener {

    private TarantulaLogger logger;
    private UDPEndpointServiceProvider udpEndpointServiceProvider;
    private ServiceContext serviceContext;
    private TokenValidatorProvider tokenValidator;
    private int channelId;
    private int sessionId;
    private byte[] key;
    private Connection connection;

    private ConcurrentHashMap<Integer,UDPChannel> channels;

    public UDPEndpoint(){
        channels = new ConcurrentHashMap<>();
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

    public Channel register(String systemId, UDPEndpointServiceProvider.RequestListener requestListener){
        UserChannel userChannel = new UserChannel(channelId++,udpEndpointServiceProvider,this,this,this);
        udpEndpointServiceProvider.registerUserChannel(userChannel);
        UDPChannel uch = new UDPChannel(this.connection,userChannel,sessionId++,key,requestListener);
        channels.put(uch.sessionId(),uch);
        return uch;
    }

    @Override
    public void onTimeout(int channelId, int sessionId) {
        logger.warn("Session->["+sessionId+"] timed out from channel ["+channelId+"]");
        channels.remove(sessionId);
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
    public byte[] onMessage(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        //logger.warn(messageHeader.toString()+">>"+messageHeader.commandId);
        UDPChannel udpChannel = channels.get(messageHeader.sessionId);
        if(messageHeader.encrypted){
            try{
                Cipher cipher = CipherUtil.decrypt(key);
                byte[] plain = cipher.doFinal(messageBuffer.readPayload());
                messageBuffer.reset();
                messageBuffer.writeHeader(messageHeader);
                messageBuffer.writePayload(plain);
                messageBuffer.flip();
                messageBuffer.readHeader();
                udpChannel.onMessage(messageHeader,messageBuffer);

            }catch (Exception ex){
                logger.error("error",ex);
            }
        }
        else{
            udpChannel.onMessage(messageHeader,messageBuffer);
        }
        return null;
    }
}
