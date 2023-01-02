package com.tarantula.cci.udp;

import com.icodesoftware.*;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.protocol.UDPOperationSummary;
import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.CipherUtil;
import com.tarantula.platform.ClientConnection;
import com.tarantula.platform.service.metrics.PerformanceMetrics;

import javax.crypto.Cipher;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;


public class UDPEndpoint implements EndPoint , UDPEndpointServiceProvider.SessionListener,UDPEndpointServiceProvider.UserSessionValidator,UDPEndpointServiceProvider.RequestListener ,SchedulingTask{

    private static final String CONFIG = "push-service-settings";
    private TarantulaLogger logger;
    private UDPEndpointServiceProvider udpEndpointServiceProvider;

    private ConcurrentLinkedDeque<PushUserChannel> pushUserChannels;
    private TokenValidatorProvider tokenValidator;
    private AtomicInteger sessionId;
    private byte[] key;
    private Connection connection;
    private String host;
    private String threadPoolSetting;

    private ConcurrentHashMap<Integer,UDPChannel> channels;
    private ConcurrentLinkedDeque<UDPChannel> pendingQueue;
    private MetricsListener metricsListener;


    private Thread receiverDaemon;
    private Thread outboundMessageDaemon;

    private ServiceContext serviceContext;
    private boolean running = true;
    private int channelPoolSize;
    private int frameRate = UDPEndpointServiceProvider.FRAME_RATE;

    public UDPEndpoint(){
        channels = new ConcurrentHashMap<>();
        pendingQueue = new ConcurrentLinkedDeque<>();
        connection = new ClientConnection();
        sessionId = new AtomicInteger(1);
    }
    public void setup(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
        Configuration cfg = serviceContext.configuration(CONFIG);
        this.tokenValidator = (TokenValidatorProvider) serviceContext.serviceProvider(TokenValidatorProvider.NAME);
        logger = serviceContext.logger(UDPEndpoint.class);
        this.pushUserChannels = new ConcurrentLinkedDeque<>();
        String udpProvider = (String)cfg.property("udpEndpointServiceProvider");
        this.udpEndpointServiceProvider = createInstance(udpProvider);
        this.udpEndpointServiceProvider.address(host);
        this.udpEndpointServiceProvider.port(connection.port());
        this.udpEndpointServiceProvider.inboundThreadPoolSetting(threadPoolSetting);
        this.channelPoolSize = ((Number)cfg.property("channelPoolSize")).intValue();
        this.key = serviceContext.deploymentServiceProvider().serverKey("pushChannel");
        connection.serverId(UUID.randomUUID().toString());
        connection.type(Connection.UDP);
        connection.secured(true);
        connection.host(serviceContext.node().servicePushAddress());
        frameRate = ((Number)cfg.property("frameRate")).intValue();
        udpEndpointServiceProvider.sessionTimeout(((Number)cfg.property("sessionTimeout")).intValue());
        udpEndpointServiceProvider.receiverTimeout(((Number)cfg.property("receiverTimeout")).intValue());
        udpEndpointServiceProvider.retryInterval(((Number)cfg.property("retryInterval")).intValue());
        udpEndpointServiceProvider.pingListenerInterval(((Number)cfg.property("pingListenerInterval")).intValue());
        udpEndpointServiceProvider.pingClientInterval(((Number)cfg.property("pingClientInterval")).intValue());
        receiverDaemon = new Thread(()->{
            while (running){
                try {
                    if(!udpEndpointServiceProvider.onReceiveMessage()){
                        Thread.sleep(UDPEndpointServiceProvider.SLEEP_TIME_OUT);
                    }
                }catch (Exception ex){
                    //ignore
                }
            }
        },"tarantula-udp-message-receiver");
        outboundMessageDaemon = new Thread(()->{
            while (running){
                try {
                    if(!udpEndpointServiceProvider.onOutboundMessage()){
                        Thread.sleep(UDPEndpointServiceProvider.SLEEP_TIME_OUT);
                    }
                }catch (Exception ex){
                    //ignore
                }
            }
        },"tarantula-udp-outbound-message-sender");
        for(int i=1;i<=channelPoolSize;i++) {
            PushUserChannel pushUserChannel = new PushUserChannel(i, udpEndpointServiceProvider, this, this, this);
            pushUserChannels.offer(pushUserChannel);
        }
        int sessionPoolSize = ((Number)cfg.property("sessionPoolSize")).intValue();
        for(int i=0;i<sessionPoolSize;i++){
            PushUserChannel pushUserChannel = pushUserChannels.poll();
            pendingQueue.offer(new UDPChannel(connection,pushUserChannel,key,udpEndpointServiceProvider.sessionTimeout()));
            pushUserChannels.offer(pushUserChannel);
        }
        logger.warn("UDP Endpoint running as a daemon with session pool size ["+sessionPoolSize+"] on ["+serviceContext.node().servicePushAddress()+"]");
    }

    @Override
    public void start() throws Exception {
        udpEndpointServiceProvider.start();
        receiverDaemon.setPriority(8);
        receiverDaemon.start();
        outboundMessageDaemon.setPriority(8);
        outboundMessageDaemon.start();
        serviceContext.schedule(this);
        PushUserChannel pushUserChannel;
        do{
            pushUserChannel = pushUserChannels.poll();
            if(pushUserChannel!=null) this.udpEndpointServiceProvider.registerUserChannel(pushUserChannel);
        }while (pushUserChannel!=null);
    }

    @Override
    public void shutdown() throws Exception {
        this.running = false;
        if(udpEndpointServiceProvider==null) return;
        udpEndpointServiceProvider.shutdown();
    }

    @Override
    public String name() {
        return EndPoint.UDP_ENDPOINT;
    }

    @Override
    public void address(String address) {
        this.host = address;
    }

    @Override
    public void port(int port) {
        this.connection.port(port);
    }

    @Override
    public void inboundThreadPoolSetting(String poolSetting) {
        this.threadPoolSetting = poolSetting;
    }

    public Channel register(Session session, UDPEndpointServiceProvider.RequestListener requestListener,Session.TimeoutListener timeoutListener){
        UDPChannel uch = this.pendingQueue.poll();
        uch.register(session,sessionId.getAndIncrement(),requestListener,timeoutListener);
        channels.put(uch.sessionId(),uch);
        return uch;
    }
    public Channel channel(int sessionId){
        return channels.get(sessionId);
    }

    @Override
    public void onTimeout(int channelId, int sessionId) {
        UDPChannel removed = channels.remove(sessionId);
        if(removed == null) return;
        pendingQueue.offer(removed);
        removed.kickoff();
    }

    @Override
    public boolean validate(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        try{
            if(!messageHeader.encrypted) return false;
            Cipher cipher = CipherUtil.decrypt(key);
            byte[] buffer = udpEndpointServiceProvider.buffer();
            int length = messageBuffer.readPayload(buffer);
            byte[] plain = cipher.doFinal(buffer,0,length);
            udpEndpointServiceProvider.buffer(buffer);
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
            metricsListener.onUpdated(PerformanceMetrics.PERFORMANCE_UDP_REQUEST_COUNT,1);
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
                byte[] buffer = udpEndpointServiceProvider.buffer();
                int length = messageBuffer.readPayload(buffer);
                byte[] plain = cipher.doFinal(buffer,0,length);
                udpEndpointServiceProvider.buffer(buffer);
                messageBuffer.reset();
                messageBuffer.writeHeader(messageHeader);
                messageBuffer.writePayload(plain);
                messageBuffer.flip();
                messageBuffer.readHeader();
                udpChannel.onMessage(messageHeader,messageBuffer);
                metricsListener.onUpdated(PerformanceMetrics.PERFORMANCE_UDP_REQUEST_COUNT,1);
            }catch (Exception ex){
                logger.error("error on message",ex);
            }
        }
        else{
            udpChannel.onMessage(messageHeader,messageBuffer);
            metricsListener.onUpdated(PerformanceMetrics.PERFORMANCE_UDP_REQUEST_COUNT,1);
        }
        return null;
    }



    @Override
    public void registerMetricsListener(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }

    @Override
    public void registerSummary(Summary summary){
        udpEndpointServiceProvider.registerSummary(summary);
        summary.registerCategory(UDPOperationSummary.PENDING_UDP_SESSION_SIZE);
        summary.registerCategory(UDPOperationSummary.UDP_GAME_SESSION_SIZE);
    }
    @Override
    public void updateSummary(Summary summary){
        udpEndpointServiceProvider.updateSummary(summary);
        summary.update(UDPOperationSummary.PENDING_UDP_SESSION_SIZE,pendingQueue.size());
        summary.update(UDPOperationSummary.UDP_GAME_SESSION_SIZE,channels.size());
    }

    @Override
    public boolean oneTime() {
        return true;
    }

    @Override
    public long initialDelay() {
        return frameRate;
    }

    @Override
    public long delay() {
        return 0;
    }

    @Override
    public void run() {
        udpEndpointServiceProvider.onTimer(frameRate);
        this.serviceContext.schedule(this);
    }

    private UDPEndpointServiceProvider createInstance(String className){
        try{
            return (UDPEndpointServiceProvider) Class.forName(className).getConstructor().newInstance();
        }catch (Exception ex){
            throw new RuntimeException("udp provider ["+className+"] not existed");
        }
    }
}
