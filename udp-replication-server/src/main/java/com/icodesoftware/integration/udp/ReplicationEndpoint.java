package com.icodesoftware.integration.udp;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.CipherUtil;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.protocol.ValidationUtil;
import com.tarantula.game.blackjack.BlackjackModule;

import javax.crypto.Cipher;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class ReplicationEndpoint implements Serviceable,UDPEndpointServiceProvider.UserSessionValidator,UDPEndpointServiceProvider.SessionListener,UDPEndpointServiceProvider.PingListener, UDPEndpointServiceProvider.RequestListener {

    private TarantulaLogger logger = JDKLogger.getLogger(ReplicationEndpoint.class);

    private UDPEndpointServiceProvider udpEndpointServiceProvider;
    private UDPEndpointServiceProvider.RequestListener gameModule;
    private byte[] serverKey;
    private String accessKey;
    private String typeId;
    private String serverId;
    private int maxChannelSize;
    private int roomCapacity;
    private AtomicInteger sessionIdSync;
    private MessageDigest messageDigest;


    private HttpCaller httpCaller;
    private String registerPath;
    private JsonObject connection;

    private JsonObject config;

    private ConcurrentHashMap<Integer,ActiveChannel> activeChannelIndex;
    private ConcurrentLinkedDeque<ActiveChannel> pendingActiveChannelQueue;
    private int pingRetries;
    private String[] headers = new String[]{
            Session.TARANTULA_ACCESS_KEY,
            "",
            Session.TARANTULA_SERVER_ID,
            "",
            Session.TARANTULA_ACTION,
            "",
    };

    private Thread receiver;
    private Thread sender;
    private boolean running = true;
    private Thread timer;
    public ReplicationEndpoint(JsonObject config){
        this.config = config;
    }


    @Override
    public void start() throws Exception {
        this.activeChannelIndex = new ConcurrentHashMap<>();
        this.pendingActiveChannelQueue = new ConcurrentLinkedDeque<>();
        this.messageDigest = MessageDigest.getInstance(TokenValidatorProvider.MDA);
        this.serverId = UUID.randomUUID().toString();
        this.sessionIdSync = new AtomicInteger(1);
        this.udpEndpointServiceProvider = (UDPEndpointServiceProvider)Class.forName(config.get("endpointServiceProvider").getAsString()).getConstructor().newInstance();
        this.udpEndpointServiceProvider.address(config.get("binding").getAsString());
        this.udpEndpointServiceProvider.port(config.get("port").getAsInt());
        this.udpEndpointServiceProvider.inboundThreadPoolSetting(config.get("inboundThreadPoolSetting").getAsString());
        this.udpEndpointServiceProvider.registerPingListener(this);
        JsonObject register = config.getAsJsonObject("register");
        this.accessKey = register.get("accessKey").getAsString();
        this.registerPath = register.get("path").getAsString();
        this.maxChannelSize = config.get("maxChannelSize").getAsInt();
        this.connection = config.getAsJsonObject("connection");
        this.httpCaller = new HttpCaller(register.get("url").getAsString());
        this.httpCaller._init();
        headers[1]=accessKey;
        headers[3]=serverId;
        headers[5]="onConnect";
        connection.addProperty("serverId",serverId);
        String resp = httpCaller.post(registerPath,connection.toString().getBytes(),headers);
        JsonObject jo = JsonUtil.parse(resp);
        if(!jo.get("successful").getAsBoolean()) throw new RuntimeException(resp);
        this.serverKey = Base64.getDecoder().decode(jo.get("serverKey").getAsString());
        this.typeId = jo.get("typeId").getAsString();
        this.udpEndpointServiceProvider.sessionTimeout(jo.get("sessionTimeout").getAsInt());
        this.roomCapacity = jo.get("capacity").getAsInt();
        int channelRegistered =0;
        for(int i=0;i<maxChannelSize;i++){
            if(createChannel()) channelRegistered++;
        }
        if(channelRegistered==0){
            shutdown();
            throw new RuntimeException("channel cannot be registered");
        }
        headers[5]="onStart";
        resp = httpCaller.post(registerPath,connection.toString().getBytes(),headers);
        jo = JsonUtil.parse(resp);
        if(!jo.get("successful").getAsBoolean()) throw new RuntimeException(resp);
        receiver = new Thread(()->{
                while (running){
                    try {
                        if(!udpEndpointServiceProvider.onReceiveMessage()){
                            Thread.sleep(UDPEndpointServiceProvider.SLEEP_TIME_OUT);
                        }
                    }catch (Exception ex){
                        //ignore
                    }}
                },"tarantula-udp-message-receiver");
        sender = new Thread(()->{
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
        timer = new Thread(()->{
            while(running){
                try{
                    Thread.sleep(UDPEndpointServiceProvider.FRAME_RATE);
                    udpEndpointServiceProvider.onTimer(UDPEndpointServiceProvider.FRAME_RATE);
                }catch (Exception ex){
                    //ignore
                }
            }
        },"tarantula-udp-message-timer");
        this.udpEndpointServiceProvider.start();
        receiver.setPriority(UDPEndpointServiceProvider.RECEIVER_THREAD_PRIORITY);
        receiver.start();
        sender.setPriority(UDPEndpointServiceProvider.SENDER_THREAD_PRIORITY);
        sender.start();
        timer.start();
        gameModule = new BlackjackModule();
        logger.warn("Game server is running on ["+typeId+"] configured with capacity ["+roomCapacity+"] Session Time ["+udpEndpointServiceProvider.sessionTimeout()+"] channels registered ["+channelRegistered+"/"+maxChannelSize+"]");
    }

    @Override
    public void shutdown() throws Exception {
        headers[5]="onStop";
        logger.warn(httpCaller.post(registerPath,connection.toString().getBytes(),headers));
        this.running = false;
        this.udpEndpointServiceProvider.shutdown();
    }



    @Override
    public void onTimeout(int channelId, int sessionId) {
        ActiveChannel activeChannel = activeChannelIndex.get(channelId);
        if(activeChannel.totalJoined.decrementAndGet()==0){
            //pendingActiveChannelQueue.offer(activeChannel);
            logger.warn("timeout->"+channelId+">>"+sessionId);
            if(createChannel()){
               logger.warn("channel created");
            }
        }
    }

    @Override
    public boolean validate(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        try{
            if(!messageHeader.encrypted) return false;
            Cipher cipher = CipherUtil.decrypt(serverKey);
            byte[] plain = cipher.doFinal(messageBuffer.readPayload());
            messageBuffer.reset();
            messageBuffer.writeHeader(messageHeader);
            messageBuffer.writePayload(plain);
            messageBuffer.flip();
            messageBuffer.readHeader();
            int sessionId = messageBuffer.readInt();
            String token = messageBuffer.readUTF8();
            String ticket = messageBuffer.readUTF8();
            MessageDigest mda = (MessageDigest)messageDigest.clone();
            ValidationUtil.Token session = ValidationUtil.validToken(mda,token);
            boolean suc = ValidationUtil.validTicket(mda,session.systemId,session.stub,ticket)!=null;
            if(suc&&sessionId==messageHeader.sessionId){
                activeChannelIndex.get(messageHeader.channelId).totalJoined.incrementAndGet();
                return true;
            }
            return false;
        }catch (Exception ex){
            logger.error("unexpected error on validate",ex);
            return false;
        }
    }

    @Override
    public void onPing() {
        if(pingRetries>UDPEndpointServiceProvider.CONNECTION_HEALTHY_CHECK_RETRIES) return;
        ActiveChannel activeChannel = pendingActiveChannelQueue.poll();
        try{
            if(activeChannel==null) {
                ping();
            }
            else{
                if(ping()){
                    //headers[5]="onChannel";
                    //JsonObject ret = JsonUtil.parse(httpCaller.post(registerPath,activeChannel.payload,headers));
                    //if(!ret.get("successful").getAsBoolean()) pendingActiveChannelQueue.offer(activeChannel);
                    //createChannel();
                }
            }
        }catch (Exception ex){
            logger.error("unexpected error on ->"+registerPath+"/"+headers[5]+"/"+headers[3],ex);
            if(activeChannel!=null) pendingActiveChannelQueue.offer(activeChannel);
        }
    }
    private boolean createChannel(){
        try {
            JsonObject channel = new JsonObject();
            int sessionId = sessionIdSync.getAndIncrement();
            channel.addProperty("channelId",sessionId);
            ActiveChannel activeChannel = new ActiveChannel(channel.toString().getBytes());
            headers[5]="onChannel";
            String resp = httpCaller.post(registerPath,activeChannel.payload,headers);
            JsonObject jo = JsonUtil.parse(resp);
            if(!jo.get("successful").getAsBoolean()) return false;
            activeChannelIndex.put(sessionId, activeChannel);
            udpEndpointServiceProvider.registerUserChannel(new GameUserChannel(sessionId, udpEndpointServiceProvider, this, this,this));
            return true;
        }catch (Exception ex){
            logger.error("error on create channel",ex);
            return false;
        }
    }
    private boolean ping() throws Exception{
        headers[5]="onPing";
        boolean suc = JsonUtil.parse(httpCaller.get(registerPath,headers)).get("successful").getAsBoolean();
        pingRetries = suc?0:pingRetries+1;
        return suc;
    }

    @Override
    public byte[] onMessage(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        //logger.warn("on request");
        return gameModule.onMessage(messageHeader,messageBuffer);
    }
}
