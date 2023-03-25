package com.tarantula.cci.udp;

import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.EndPoint;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.CipherUtil;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.ClientConnection;
import com.tarantula.platform.ScheduleRunner;
import com.tarantula.platform.service.metrics.PerformanceMetrics;

import javax.crypto.Cipher;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.atomic.AtomicInteger;

public class UDPEndpoint implements EndPoint , UDPEndpointServiceProvider.SessionListener,UDPEndpointServiceProvider.UserSessionValidator,UDPEndpointServiceProvider.RequestListener,UDPEndpointServiceProvider.ActionListener, UDPEndpointServiceProvider.CipherListener {

    private static final String CONFIG = "push-service-settings";
    private TarantulaLogger logger;
    private UDPEndpointServiceProvider udpEndpointServiceProvider;

    private ArrayBlockingQueue<PushUserChannel> pendingUserChannels;
    private ConcurrentHashMap<Integer,ActivePushChannel> activePushChannelIndex;
    private TokenValidatorProvider tokenValidator;
    private AtomicInteger channelId;
    private AtomicInteger sessionId;
    private byte[] key;
    private Connection connection;
    private String host;
    private String threadPoolSetting;

    private ConcurrentHashMap<Integer,UDPChannel> channels;
    private ArrayList<MessageBuffer.MessageHeader> expiredPackets;
    private ConcurrentHashMap<MessageBuffer.MessageHeader,PacketTrack> packetTracks;
    private long packetRemoveInterval;
    private long packetTimeout;
    private long packetRemoveTimer;
    private MetricsListener metricsListener;


    private Thread receiverDaemon;
    private Thread outboundMessageDaemon;

    private ServiceContext serviceContext;
    private boolean running = true;
    private AtomicInteger channelPoolSize;
    private int frameRate = UDPEndpointServiceProvider.FRAME_RATE;

    private long sessionJoinTimeout;
    private long sessionJoinTimer = 1000;
    private SchedulingTask onTimer;
    private ArrayList<Channel> pendingJoinKickoff;
    private ConcurrentHashMap<Integer,PendingJoinChannel> pendingJoins;

    private int sessionTimeout;
    private UDPOperationSummary operationSummary;

    public UDPEndpoint(){
        channels = new ConcurrentHashMap<>();
        activePushChannelIndex = new ConcurrentHashMap<>();
        pendingJoins = new ConcurrentHashMap<>();
        pendingJoinKickoff = new ArrayList<>();
        packetTracks = new ConcurrentHashMap<>();
        expiredPackets = new ArrayList<>();
        connection = new ClientConnection();
        channelId = new AtomicInteger(1);
        sessionId = new AtomicInteger(1);
        operationSummary = new UDPOperationSummary();
    }
    public void setup(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
        Configuration cfg = serviceContext.configuration(CONFIG);
        this.packetTimeout = ((Number)cfg.property("packetTimeoutInSeconds")).longValue();
        this.packetRemoveInterval = ((Number)cfg.property("packetRemoveInterval")).longValue();
        this.packetRemoveTimer = this.packetRemoveInterval;
        this.tokenValidator = (TokenValidatorProvider) serviceContext.serviceProvider(TokenValidatorProvider.NAME);
        logger = serviceContext.logger(UDPEndpoint.class);
        String udpProvider = (String)cfg.property("udpEndpointServiceProvider");
        this.udpEndpointServiceProvider = createInstance(udpProvider);
        this.udpEndpointServiceProvider.address(host);
        this.udpEndpointServiceProvider.port(connection.port());
        this.udpEndpointServiceProvider.inboundThreadPoolSetting(threadPoolSetting);
        int _channelPoolSize = ((Number)cfg.property("channelPoolSize")).intValue();
        this.channelPoolSize = new AtomicInteger(_channelPoolSize);
        this.pendingUserChannels = new ArrayBlockingQueue<>(_channelPoolSize);
        this.key = serviceContext.deploymentServiceProvider().serverKey("pushChannel");
        connection.serverId(UUID.randomUUID().toString());
        connection.type(Connection.UDP);
        connection.secured(true);
        connection.host(serviceContext.node().servicePushAddress());
        frameRate = ((Number)cfg.property("frameRate")).intValue();
        this.sessionJoinTimeout = ((Number)cfg.property("sessionJoinTimeout")).longValue();
        this.sessionTimeout = ((Number)cfg.property("sessionTimeout")).intValue();
        udpEndpointServiceProvider.sessionTimeout(sessionTimeout);
        udpEndpointServiceProvider.receiverTimeout(((Number)cfg.property("receiverTimeout")).intValue());
        udpEndpointServiceProvider.retryInterval(((Number)cfg.property("retryInterval")).intValue());
        udpEndpointServiceProvider.pingListenerInterval(((Number)cfg.property("pingListenerInterval")).intValue());
        udpEndpointServiceProvider.pingClientInterval(((Number)cfg.property("pingClientInterval")).intValue());
        udpEndpointServiceProvider.registerCipherListener(this);
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
        logger.warn("UDP Endpoint running as a daemon with channel pool size ["+channelPoolSize+"] on ["+serviceContext.node().servicePushAddress()+"]");
    }

    @Override
    public void start() throws Exception {
        udpEndpointServiceProvider.start();
        receiverDaemon.setPriority(UDPEndpointServiceProvider.RECEIVER_THREAD_PRIORITY);
        receiverDaemon.start();
        outboundMessageDaemon.setPriority(UDPEndpointServiceProvider.SENDER_THREAD_PRIORITY);
        outboundMessageDaemon.start();
        this.onTimer = new ScheduleRunner(this.frameRate,()->{
            this.onTimer();
        });
        serviceContext.schedule(onTimer);
        this.serviceContext.deploymentServiceProvider().onStart(this);
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

    public Channel channel(int sessionId){
        return channels.get(sessionId);
    }

    public UDPChannel[] createChannels(int capacity) {
        PushUserChannel pushUserChannel = pendingUserChannels.poll();
        if (pushUserChannel == null && channelPoolSize.decrementAndGet()<0) return new UDPChannel[0];

        if(pushUserChannel == null) {
            pushUserChannel = new PushUserChannel(channelId.getAndIncrement(), udpEndpointServiceProvider,this, this, this, this,this);
            this.activePushChannelIndex.put(pushUserChannel.channelId(),new ActivePushChannel());
            operationSummary.userChannelNumber.incrementAndGet();
        }
        UDPChannel[] channels = new UDPChannel[capacity];
        for(int i=0;i<capacity;i++){
            UDPChannel channel = new UDPChannel(connection,pushUserChannel,key,udpEndpointServiceProvider.sessionTimeout(),this);
            channels[i] = channel;
        }
        operationSummary.userSessionNumber.addAndGet(capacity);
        this.udpEndpointServiceProvider.registerUserChannel(pushUserChannel);
        this.activePushChannelIndex.get(pushUserChannel.channelId()).reset(capacity);
        return channels;
    }

    public void registerChannel(UDPChannel channel){
        channel.sessionId(sessionId.getAndIncrement());
        channels.put(channel.sessionId(),channel);
        pendingJoins.put(channel.sessionId,new PendingJoinChannel(channel,sessionJoinTimeout));
    }

    @Override
    public void onLeft(int channelId, int sessionId) {
        //logger.warn("Session left->"+sessionId+">>"+channelId);
        ActivePushChannel activePushChannel = activePushChannelIndex.get(channelId);
        if(activePushChannel.totalLeft.decrementAndGet()==0){
            PushUserChannel released = (PushUserChannel) udpEndpointServiceProvider.releaseUserChannel(channelId);
            pendingUserChannels.offer(released);
        }
        UDPChannel removed = channels.remove(sessionId);
        if(removed == null) return;
        removed.kickoff();
    }

    @Override
    public void onJoined(int channelId, int sessionId){
        //logger.warn("Session joined->"+sessionId+">>"+channelId);
        UDPChannel joined = channels.get(sessionId);
        joined.joined();
    }

    @Override
    public boolean validate(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        try{
            int sessionId = messageBuffer.readInt();
            String token = messageBuffer.readUTF8();
            String ticket = messageBuffer.readUTF8();
            OnSession session = tokenValidator.tokenValidator().validateToken(token);
            boolean suc = tokenValidator.validateTicket(session.systemId(),session.stub(),ticket);
            metricsListener.onUpdated(PerformanceMetrics.PERFORMANCE_UDP_REQUEST_COUNT,1);
            boolean joined = sessionId == messageHeader.sessionId && suc;
            return  joined && pendingJoins.remove(sessionId)!=null;
        }catch (Exception ex){
            logger.error("unexpected error on validate",ex);
            return false;
        }
    }

    @Override
    public byte[] onRequest(Session session,MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        //logger.warn("Message header->"+messageHeader.toString()+">>"+messageHeader.commandId+">"+messageHeader.encrypted);
        PacketTrack packetTrack = packetTracks.compute(messageHeader.copy(),(k,v)->{
            if(v==null) v = new PacketTrack(packetTimeout);
            v.count++;
            return v;
        });
        if(packetTrack.count>1){
            return null;
        }
        UDPChannel udpChannel = channels.get(messageHeader.sessionId);
        udpChannel.onRequest(messageHeader,messageBuffer);
        metricsListener.onUpdated(PerformanceMetrics.PERFORMANCE_UDP_REQUEST_COUNT,1);
        return null;
    }



    @Override
    public void registerMetricsListener(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }

    @Override
    public void registerSummary(Summary summary){
        udpEndpointServiceProvider.registerSummary(summary);
        summary.registerCategory(UDPOperationSummary.USER_CHANNEL_NUMBER);
        summary.registerCategory(UDPOperationSummary.USER_SESSION_NUMBER);
    }
    @Override
    public void updateSummary(Summary summary){
        udpEndpointServiceProvider.updateSummary(summary);
        summary.update(UDPOperationSummary.USER_CHANNEL_NUMBER,operationSummary.userChannelNumber.get());
        summary.update(UDPOperationSummary.USER_SESSION_NUMBER,operationSummary.userSessionNumber.get());
    }

    private void onTimer() {
        udpEndpointServiceProvider.onTimer(frameRate);
        packetRemoveTimer -= frameRate;
        sessionJoinTimer -= frameRate;
        if(packetRemoveTimer<=0){
            expiredPackets.clear();
            packetTracks.forEach((k,v)->{
                if(TimeUtil.expired(v.creationTime)){
                    expiredPackets.add(k);
                }
            });
            if(expiredPackets.size()>0){
                expiredPackets.forEach((h)->packetTracks.remove(h));
                //logger.warn("Total packets removed ->"+expiredPackets.size());
            }
            packetRemoveTimer = packetRemoveInterval;
        }
        if(sessionJoinTimer<=0){
            sessionJoinTimer = 1000;
            pendingJoinKickoff.clear();
            pendingJoins.forEach((k,v)->{
                if(v.timeout.addAndGet(-1*1000)<=0){
                    pendingJoinKickoff.add(v.channel);
                }
            });
            pendingJoinKickoff.forEach(c->{
                if(pendingJoins.remove(c.sessionId())!=null){
                    onLeft(c.channelId(),c.sessionId());
                }
            });
        }
        this.serviceContext.schedule(onTimer);
    }

    public int sessionTimeout(){
        return this.sessionTimeout;
    }


    @Override
    public void onAction(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback) {
        //logger.warn("Message header->"+messageHeader.toString()+">>"+messageHeader.commandId+">"+messageHeader.encrypted);
        UDPChannel channel = channels.get(messageHeader.sessionId);
        channel.onAction(messageHeader,messageBuffer,callback);
    }

    public boolean decrypt(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
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
            return true;
        }catch (Exception ex){
            logger.error("invalid message",ex);
            return false;
        }
    }
    public boolean encrypt(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        try{
            Cipher cipher = CipherUtil.encrypt(key);
            byte[] buffer = udpEndpointServiceProvider.buffer();
            int length = messageBuffer.readPayload(buffer);
            byte[] encrypt = cipher.doFinal(buffer,0,length);
            if(encrypt.length > MessageBuffer.PAYLOAD_SIZE) throw new RuntimeException("over sized payload ["+encrypt.length+"]");
            udpEndpointServiceProvider.buffer(buffer);
            messageBuffer.reset();
            messageBuffer.writeHeader(messageHeader);
            messageBuffer.writePayload(encrypt);
            messageBuffer.flip();
            messageBuffer.readHeader();
            return true;
        }catch (Exception ex){
            logger.error("invalid message",ex);
            return false;
        }
    }

    private UDPEndpointServiceProvider createInstance(String className){
        try{
            return (UDPEndpointServiceProvider) Class.forName(className).getConstructor().newInstance();
        }catch (Exception ex){
            throw new RuntimeException("udp provider ["+className+"] not existed");
        }
    }
}
