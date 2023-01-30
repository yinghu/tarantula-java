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
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.ClientConnection;
import com.tarantula.platform.ScheduleRunner;
import com.tarantula.platform.service.metrics.PerformanceMetrics;

import javax.crypto.Cipher;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class UDPEndpoint implements EndPoint , UDPEndpointServiceProvider.SessionListener,UDPEndpointServiceProvider.UserSessionValidator,UDPEndpointServiceProvider.RequestListener{

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
    private int channelPoolSize;
    private int frameRate = UDPEndpointServiceProvider.FRAME_RATE;

    private long sessionJoinTimeout;
    private long sessionJoinTimer = 1000;
    private SchedulingTask onTimer;
    private ArrayList<Integer> pendingJoinKickoff;
    private ConcurrentHashMap<Integer,AtomicLong> pendingJoins;

    private int sessionTimeout;

    public UDPEndpoint(){
        channels = new ConcurrentHashMap<>();
        pendingJoins = new ConcurrentHashMap<>();
        pendingJoinKickoff = new ArrayList<>();
        pendingQueue = new ConcurrentLinkedDeque<>();
        packetTracks = new ConcurrentHashMap<>();
        expiredPackets = new ArrayList<>();
        connection = new ClientConnection();
        sessionId = new AtomicInteger(1);
    }
    public void setup(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
        Configuration cfg = serviceContext.configuration(CONFIG);
        this.packetTimeout = ((Number)cfg.property("packetTimeoutInSeconds")).longValue();
        this.packetRemoveInterval = ((Number)cfg.property("packetRemoveInterval")).longValue();
        this.packetRemoveTimer = this.packetRemoveInterval;
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
        this.sessionJoinTimeout = ((Number)cfg.property("sessionJoinTimeout")).longValue();
        this.sessionTimeout = ((Number)cfg.property("sessionTimeout")).intValue();
        udpEndpointServiceProvider.sessionTimeout(sessionTimeout);
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
        receiverDaemon.setPriority(UDPEndpointServiceProvider.RECEIVER_THREAD_PRIORITY);
        receiverDaemon.start();
        outboundMessageDaemon.setPriority(UDPEndpointServiceProvider.SENDER_THREAD_PRIORITY);
        outboundMessageDaemon.start();
        this.onTimer = new ScheduleRunner(this.frameRate,()->{
            this.onTimer();
        });
        serviceContext.schedule(onTimer);
        PushUserChannel pushUserChannel;
        do{
            pushUserChannel = pushUserChannels.poll();
            if(pushUserChannel!=null) this.udpEndpointServiceProvider.registerUserChannel(pushUserChannel);
        }while (pushUserChannel!=null);
        this.serviceContext.deploymentServiceProvider().onStart(this);
    }

    @Override
    public void shutdown() throws Exception {
        this.running = false;
        if(udpEndpointServiceProvider==null) return;
        udpEndpointServiceProvider.shutdown();
        this.serviceContext.deploymentServiceProvider().onStop(this);
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
        pendingJoins.put(uch.sessionId(),new AtomicLong(sessionJoinTimeout));
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
            boolean joined = sessionId==messageHeader.sessionId && suc;
            return  joined && pendingJoins.remove(sessionId)!=null;
        }catch (Exception ex){
            logger.error("unexpected error on validate",ex);
            return false;
        }
    }

    @Override
    public byte[] onMessage(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        //logger.warn("Message header->"+messageHeader.toString());
        PacketTrack packetTrack = packetTracks.compute(messageHeader.copy(),(k,v)->{
            if(v==null) v = new PacketTrack(packetTimeout);
            v.count++;
            return v;
        });
        if(packetTrack.count>1){
            return null;
        }
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
                if(v.addAndGet(-1*1000)<=0){
                    pendingJoinKickoff.add(k);
                }
            });
            pendingJoinKickoff.forEach(k->{
                if(pendingJoins.remove(k)!=null){
                    onTimeout(0,k);
                }
            });
        }
        this.serviceContext.schedule(onTimer);
    }

    public int sessionTimeout(){
        return this.sessionTimeout;
    }

    private UDPEndpointServiceProvider createInstance(String className){
        try{
            return (UDPEndpointServiceProvider) Class.forName(className).getConstructor().newInstance();
        }catch (Exception ex){
            throw new RuntimeException("udp provider ["+className+"] not existed");
        }
    }
}
