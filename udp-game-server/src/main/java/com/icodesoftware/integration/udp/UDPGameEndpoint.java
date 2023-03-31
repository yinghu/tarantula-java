package com.icodesoftware.integration.udp;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.JsonUtil;
import com.icodesoftware.util.ScheduleRunner;
import com.icodesoftware.util.TarantulaExecutorServiceFactory;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class UDPGameEndpoint implements Serviceable,UDPEndpointServiceProvider.UserSessionValidator,UDPEndpointServiceProvider.SessionListener,UDPEndpointServiceProvider.PingListener, UDPEndpointServiceProvider.ActionListener,GameContext{

    private TarantulaLogger logger = JDKLogger.getLogger(UDPGameEndpoint.class);

    private ScheduledExecutorService scheduledExecutorService;
    private ScheduleRunner timer;

    private UDPEndpointServiceProvider udpEndpointServiceProvider;
    //private GameModule gameModule;
    private String moduleName;
    private ActiveRoom room;

    private UDPEndpointServiceProvider.CipherListener cipherListener;
    private String accessKey;
    private String typeId;
    private String serverId;
    private int maxChannelSize;

    private AtomicInteger keySync;

    private MessageDigest messageDigest;


    private HttpCaller httpCaller;
    private String registerPath;
    private JsonObject connection;

    private JsonObject config;

    private ConcurrentHashMap<Integer, ActiveGame> activeGameIndex;
    //private ConcurrentLinkedDeque<ActiveGameChannel> pendingActiveChannelQueue;
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

    public UDPGameEndpoint(JsonObject config){
        this.config = config;
    }


    @Override
    public void start() throws Exception {
        this.scheduledExecutorService = TarantulaExecutorServiceFactory.createScheduledExecutorService(config.get("schedulerSetting").getAsString());
        this.activeGameIndex = new ConcurrentHashMap<>();
        this.moduleName = config.get("gameModule").getAsString();
        //this.pendingActiveChannelQueue = new ConcurrentLinkedDeque<>();
        this.messageDigest = MessageDigest.getInstance(TokenValidatorProvider.MDA);
        this.serverId = UUID.randomUUID().toString();
        this.keySync = new AtomicInteger(1);
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
        byte[] serverKey = Base64.getDecoder().decode(jo.get("serverKey").getAsString());
        this.typeId = jo.get("typeId").getAsString();
        int timeout = jo.get("sessionTimeout").getAsInt();
        this.udpEndpointServiceProvider.sessionTimeout(timeout);
        int capacity = jo.get("capacity").getAsInt();
        long duration = jo.get("duration").getAsLong();
        long overtime = jo.get("overtime").getAsLong();
        int joinsOnStart = jo.get("joinsOnStart").getAsInt();
        this.room = new ActiveRoom(capacity,duration,overtime,joinsOnStart,timeout);
        this.cipherListener = this.udpEndpointServiceProvider.registerCipherListener(serverKey);
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
        timer = new ScheduleRunner(UDPEndpointServiceProvider.FRAME_RATE,()->{
            try{
                udpEndpointServiceProvider.onTimer(UDPEndpointServiceProvider.FRAME_RATE);
            }catch (Exception ex){
                logger.error("error on timer",ex);
                //ignore
            }
            this.schedule(timer);
        });
        this.udpEndpointServiceProvider.start();
        receiver.setPriority(UDPEndpointServiceProvider.RECEIVER_THREAD_PRIORITY);
        receiver.start();
        sender.setPriority(UDPEndpointServiceProvider.SENDER_THREAD_PRIORITY);
        sender.start();
        this.schedule(timer);
        logger.warn("Game server is running with ["+typeId+"] configured with capacity ["+room.capacity()+"] Session Time ["+udpEndpointServiceProvider.sessionTimeout()+"] channels registered ["+channelRegistered+"/"+maxChannelSize+"]");
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
        ActiveGame activeGame = activeGameIndex.get(channelId);
        int totalLeft = activeGame.totalLeft.decrementAndGet();
        logger.warn("Session timeout->"+channelId+">>"+sessionId);
        activeGame.gameModule.onLeft(new ActiveChannel(sessionId));
        if(totalLeft == 0){
            logger.warn("Channel Removed->"+channelId+">>"+sessionId);
            this.udpEndpointServiceProvider.releaseUserChannel(channelId);
            this.activeGameIndex.remove(channelId);
            if(createChannel()){
               logger.warn("channel created");
            }
        }
    }

    @Override
    public void onJoined(int channelId, int sessionId){
        logger.warn("Session joined->"+channelId+">>"+sessionId);
        ActiveGame activeGame = activeGameIndex.get(channelId);
        activeGame.gameModule.onJoined(new ActiveChannel(sessionId));
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
                ActiveGame activeGame = activeGameIndex.get(messageHeader.channelId);
                activeGame.totalJoined.incrementAndGet();
                activeGame.gameModule.onValidated(new ActiveChannel(session.systemId,sessionId));
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
            channel.addProperty("sessionId",keySync.getAndAdd(this.room.capacity()));
            headers[5]="onChannel";
            String resp = httpCaller.post(registerPath,channel.toString().getBytes(),headers);
            JsonObject jo = JsonUtil.parse(resp);
            if(!jo.get("successful").getAsBoolean()) return false;
            ActiveGame activeChannel = new ActiveGame(this.createGameModule(channelId));
            activeChannel.gameModule.registerRoomListener(activeChannel);
            activeGameIndex.put(channelId, activeChannel);
            udpEndpointServiceProvider.registerUserChannel(new GameUserChannel(channelId, udpEndpointServiceProvider, this.cipherListener,this, this,this));
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
        ActiveGame activeGame = activeGameIndex.get(messageHeader.channelId);
        activeGame.gameModule.onAction(messageHeader,messageBuffer,callback);
    }


    private GameModule createGameModule(int channelId) throws Exception{
        GameModule gameModule = (GameModule)Class.forName(moduleName).getConstructor().newInstance();
        gameModule.setup(room.assign(channelId),this);
        logger.warn("Game module ["+moduleName+"] created");
        return gameModule;
    }


    //game context
    public ScheduledFuture<?> schedule(SchedulingTask task) {
        if(task.oneTime()){
            return this.scheduledExecutorService.schedule(task,task.initialDelay()+task.delay(), TimeUnit.MILLISECONDS);
        }else{
            return this.scheduledExecutorService.scheduleAtFixedRate(task,task.initialDelay(),task.delay(),TimeUnit.MILLISECONDS);
        }
    }

    public void log(String message,int level){
        switch (level){
            case OnLog.DEBUG:
                this.logger.debug(message);
                break;
            case OnLog.INFO:
                this.logger.info(message);
                break;
            case OnLog.WARN:
                this.logger.warn(message);
                break;
        }

    }

    public void log(String message,Exception error,int level){
        switch (level){
            case OnLog.WARN:
                if(error!=null){
                    this.logger.warn(message);
                }
                else{
                    this.logger.warn(message,error);
                }
                break;
            case OnLog.ERROR:
                this.logger.error(message,error);
                break;
        }
    }

    public GameServiceProxy gameServiceProxy(short serviceId) {
        return null;
    }
}
