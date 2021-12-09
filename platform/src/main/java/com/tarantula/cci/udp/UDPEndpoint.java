package com.tarantula.cci.udp;

import com.icodesoftware.*;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.UDPEndpointService;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.CipherUtil;
import com.tarantula.platform.ClientConnection;

import javax.crypto.Cipher;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;


public class UDPEndpoint implements EndPoint , UDPEndpointServiceProvider.SessionListener,UDPEndpointServiceProvider.UserSessionValidator,UDPEndpointServiceProvider.RequestListener, UDPEndpointServiceProvider.PingListener {

    private static final String CONFIG = "push-service-settings";
    private TarantulaLogger logger;
    private UDPEndpointServiceProvider udpEndpointServiceProvider;
    private PushUserChannel pushUserChannel;
    private TokenValidatorProvider tokenValidator;
    private final int singleChannelId = 1000;
    private AtomicInteger sessionId;
    private byte[] key;
    private Connection connection;

    private ConcurrentHashMap<Integer,UDPChannel> channels;
    private ConcurrentLinkedDeque<UDPChannel> pendingQueue;

    public UDPEndpoint(){
        channels = new ConcurrentHashMap<>();
        pendingQueue = new ConcurrentLinkedDeque<>();
        connection = new ClientConnection();
        udpEndpointServiceProvider = new UDPEndpointService();
        sessionId = new AtomicInteger(0);
    }
    public void setup(ServiceContext serviceContext){
        //this.serviceContext = serviceContext;
        Configuration cfg = serviceContext.configuration(CONFIG);
        this.tokenValidator = (TokenValidatorProvider) serviceContext.serviceProvider(TokenValidatorProvider.NAME);
        logger = serviceContext.logger(UDPEndpoint.class);
        this.key = serviceContext.deploymentServiceProvider().serverKey("pushChannel");
        connection.serverId(UUID.randomUUID().toString());
        connection.type(Connection.UDP);
        connection.secured(true);
        connection.host((String)cfg.property("IP"));
        udpEndpointServiceProvider.daemon(true);
        udpEndpointServiceProvider.sessionTimeout(((Number)cfg.property("sessionTimeout")).intValue());
        udpEndpointServiceProvider.receiverTimeout(((Number)cfg.property("receiverTimeout")).intValue());
        udpEndpointServiceProvider.registerPingListener(this);
        pushUserChannel = new PushUserChannel(singleChannelId,udpEndpointServiceProvider,this,this,this);
        int sessionPoolSize = ((Number)cfg.property("sessionPoolSize")).intValue();
        for(int i=0;i<sessionPoolSize;i++){
            pendingQueue.offer(new UDPChannel(connection,pushUserChannel,key,udpEndpointServiceProvider.sessionTimeout()));
        }
        logger.warn("UDP Endpoint running as a daemon with session pool size ->"+sessionPoolSize);
    }

    @Override
    public void start() throws Exception {
        udpEndpointServiceProvider.start();
        this.udpEndpointServiceProvider.registerUserChannel(pushUserChannel);
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
    public void port(int port) {
        this.udpEndpointServiceProvider.port(port);
        this.connection.port(port);
    }

    @Override
    public void inboundThreadPoolSetting(String poolSetting) {
        this.udpEndpointServiceProvider.inboundThreadPoolSetting(poolSetting);
    }

    public Channel register(String systemId, UDPEndpointServiceProvider.RequestListener requestListener,Session.TimeoutListener timeoutListener){
        UDPChannel uch = this.pendingQueue.poll();
        uch.register(systemId,sessionId.incrementAndGet(),requestListener,timeoutListener);
        channels.put(uch.sessionId(),uch);
        return uch;
    }

    @Override
    public void onTimeout(int channelId, int sessionId) {
        UDPChannel removed = channels.remove(sessionId);
        if(removed != null) {
            pendingQueue.offer(removed);
            removed.kickoff();
        }
    }

    @Override
    public boolean validate(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        try{
            if(!messageHeader.encrypted) return false;
            Cipher cipher = CipherUtil.decrypt(key);
            byte[] plain = cipher.doFinal(messageBuffer.readPayload());
            messageBuffer.reset();
            messageBuffer.writeHeader(messageHeader);
            messageBuffer.writePayload(plain);
            messageBuffer.flip();
            messageBuffer.readHeader();
            int sessionId = messageBuffer.readInt();
            String token = messageBuffer.readUTF8();
            String ticket = messageBuffer.readUTF8();
            OnSession session = tokenValidator.tokenValidator().validateToken(token);
            boolean suc = tokenValidator.validateTicket(session.systemId(),session.stub(),ticket);
            return sessionId==messageHeader.sessionId && suc;
        }catch (Exception ex){
            logger.error("unexpected error on validate",ex);
            return false;
        }
    }

    @Override
    public byte[] onMessage(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
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
                logger.error("error on message",ex);
            }
        }
        else{
            udpChannel.onMessage(messageHeader,messageBuffer);
        }
        return null;
    }

    @Override
    public void onPing() {

    }
}
