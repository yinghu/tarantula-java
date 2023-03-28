package com.icodesoftware.integration.udp;

import com.google.gson.JsonObject;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.GameModule;
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

public class UDPGameEndpoint implements Serviceable,UDPEndpointServiceProvider.UserSessionValidator,UDPEndpointServiceProvider.SessionListener,UDPEndpointServiceProvider.PingListener, UDPEndpointServiceProvider.ActionListener, UDPEndpointServiceProvider.CipherListener {

    private TarantulaLogger logger = JDKLogger.getLogger(UDPGameEndpoint.class);

    private UDPEndpointServiceProvider udpEndpointServiceProvider;
    private GameModule gameModule;
    private byte[] serverKey;
    private String accessKey;
    private String typeId;
    private String serverId;
    private int maxChannelSize;
    private int roomCapacity;

    private AtomicInteger keySync;

    private MessageDigest messageDigest;


    private HttpCaller httpCaller;
    private String registerPath;
    private JsonObject connection;

    private JsonObject config;

    private ConcurrentHashMap<Integer, ActiveGameChannel> activeChannelIndex;
    private ConcurrentLinkedDeque<ActiveGameChannel> pendingActiveChannelQueue;
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
    public UDPGameEndpoint(JsonObject config){
        this.config = config;
    }


    @Override
    public void start() throws Exception {
        this.activeChannelIndex = new ConcurrentHashMap<>();
        this.pendingActiveChannelQueue = new ConcurrentLinkedDeque<>();
        this.messageDigest = MessageDigest.getInstance(TokenValidatorProvider.MDA);
        this.serverId = UUID.randomUUID().toString();
        this.keySync = new AtomicInteger(1);
        this.udpEndpointServiceProvider = (UDPEndpointServiceProvider)Class.forName(config.get("endpointServiceProvider").getAsString()).getConstructor().newInstance();
        this.udpEndpointServiceProvider.address(config.get("binding").getAsString());
        this.udpEndpointServiceProvider.port(config.get("port").getAsInt());
        this.udpEndpointServiceProvider.inboundThreadPoolSetting(config.get("inboundThreadPoolSetting").getAsString());
        this.udpEndpointServiceProvider.registerPingListener(this);
        this.udpEndpointServiceProvider.registerCipherListener(this);
        this.createGameModule(config.get("gameModule").getAsString());
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
    public void onLeft(int channelId, int sessionId) {
        ActiveGameChannel activeChannel = activeChannelIndex.get(channelId);
        int totalLeft = activeChannel.totalLeft.decrementAndGet();
        logger.warn("Session timeout->"+channelId+">>"+sessionId);
        if(totalLeft == 0){
            logger.warn("Channel Removed->"+channelId+">>"+sessionId);
            this.udpEndpointServiceProvider.releaseUserChannel(channelId);
            this.activeChannelIndex.remove(channelId);
            if(createChannel()){
               logger.warn("channel created");
            }
        }
    }

    @Override
    public void onJoined(int channelId, int sessionId){

    }

    @Override
    public boolean validate(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        try{
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
        try{
            ping();
        }catch (Exception ex){
            logger.error("unexpected error on ->"+registerPath+"/"+headers[5]+"/"+headers[3],ex);
        }
    }
    private boolean createChannel(){
        try {
            JsonObject channel = new JsonObject();
            int channelId = keySync.getAndIncrement();
            channel.addProperty("channelId",channelId);
            channel.addProperty("sessionId",keySync.getAndAdd(roomCapacity));
            ActiveGameChannel activeChannel = new ActiveGameChannel(roomCapacity);
            headers[5]="onChannel";
            String resp = httpCaller.post(registerPath,channel.toString().getBytes(),headers);
            JsonObject jo = JsonUtil.parse(resp);
            if(!jo.get("successful").getAsBoolean()) return false;
            activeChannelIndex.put(channelId, activeChannel);
            udpEndpointServiceProvider.registerUserChannel(new GameUserChannel(channelId, udpEndpointServiceProvider, this,this, this,this));
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
    public void onAction(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer, UDPEndpointServiceProvider.RelayListener callback) {
        //logger.warn("Message header->"+messageHeader.toString()+">>"+messageHeader.commandId+">"+messageHeader.encrypted);
        this.gameModule.onAction(messageHeader,messageBuffer,callback);
    }


    public boolean decrypt(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        try{
            Cipher cipher = CipherUtil.decrypt(serverKey);
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
            Cipher cipher = CipherUtil.encrypt(serverKey);
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

    private void createGameModule(String moduleName) throws Exception{
        this.gameModule = (GameModule)Class.forName(moduleName).getConstructor().newInstance();
        this.gameModule.setup(new DedicatedRoom(),new DedicatedGameContext());
    }
}
