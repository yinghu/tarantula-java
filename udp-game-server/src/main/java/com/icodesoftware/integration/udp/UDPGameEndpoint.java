package com.icodesoftware.integration.udp;

import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.util.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class UDPGameEndpoint implements Serviceable,UDPEndpointServiceProvider.UserSessionValidator,UDPEndpointServiceProvider.SessionListener,UDPEndpointServiceProvider.PingListener, UDPEndpointServiceProvider.ActionListener,RoomListener, UDPEndpointServiceProvider.RequestListener {

    private TarantulaLogger logger = JDKLogger.getLogger(UDPGameEndpoint.class);

    private ScheduledExecutorService scheduledExecutorService;
    private ScheduleRunner timer;

    private ScheduleRunner countdownTimer;
    private UDPEndpointServiceProvider udpEndpointServiceProvider;

    private long gameModuleCountdownInterval;
    private ActiveRoom roomTemplate;

    private UDPEndpointServiceProvider.CipherListener cipherListener;
    private String accessKey;
    private String typeId;
    private String serverId;
    private int maxChannelSize;

    private AtomicInteger keySync;


    private HttpCaller httpCaller;
    private String registerPath;
    private JsonObject connection;

    private JsonObject config;

    private ConcurrentHashMap<Integer, ActiveRoom> activeGameIndex;

    private ConcurrentHashMap<Integer,ActiveChannel> activeChannelIndex;
    private ConcurrentHashMap<Integer, PendingReleaseRoom> pendingReleaseRooms;

    private int pingRetries;
    private String[] headers = new String[]{
            Session.TARANTULA_ACCESS_KEY,
            "",
            Session.TARANTULA_SERVER_ID,
            "",
            Session.TARANTULA_ACTION,
            "",
            Session.TARANTULA_NAME,
            ""
    };

    private Thread receiver;
    private Thread sender;
    private boolean running = true;

    private GameServiceProvider gameServiceProvider;
    public UDPGameEndpoint(JsonObject config){
        this.config = config;
    }


    @Override
    public void start() throws Exception {
        this.scheduledExecutorService = TarantulaExecutorServiceFactory.createScheduledExecutorService(config.get("schedulerSetting").getAsString());
        this.activeGameIndex = new ConcurrentHashMap<>();
        this.activeChannelIndex = new ConcurrentHashMap<>();
        this.pendingReleaseRooms = new ConcurrentHashMap<>();
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
        this.gameModuleCountdownInterval = config.get("gameModuleCountdownInterval").getAsLong();
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
        byte[] serverKey = Base64Util.fromBase64String(jo.get("serverKey").getAsString());
        this.typeId = jo.get("typeId").getAsString();
        int timeout = jo.get("sessionTimeout").getAsInt();
        this.udpEndpointServiceProvider.sessionTimeout(timeout);
        int capacity = jo.get("capacity").getAsInt();
        long duration = jo.get("duration").getAsLong();
        long overtime = jo.get("overtime").getAsLong();
        int joinsOnStart = jo.get("joinsOnStart").getAsInt();
        this.gameServiceProvider = toGameServiceProvider(jo.get("gameServiceProvider").getAsString());
        this.gameServiceProvider.setup(new GameServiceContext());
        logger.warn("Room setting : "+jo);
        this.roomTemplate = new ActiveRoom(capacity,duration,overtime,joinsOnStart,timeout);
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
        countdownTimer = new ScheduleRunner(gameModuleCountdownInterval,()->{
            try{
                this.onCountdown();
            }catch (Exception ex){
                logger.error("error on countdown",ex);
            }
            this.schedule(countdownTimer);
        });
        this.schedule(countdownTimer);
        logger.warn("Game server is running with ["+typeId+"] configured with capacity ["+roomTemplate.capacity()+"] Session Time ["+udpEndpointServiceProvider.sessionTimeout()+"] channels registered ["+channelRegistered+"/"+maxChannelSize+"]");
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
        ActiveRoom activeGame = activeGameIndex.get(channelId);
        int totalLeft = activeGame.leave();
        if(totalLeft == activeGame.capacity()){
            pendingReleaseRooms.remove(channelId);
        }
        logger.warn("Left :"+channelId+" : "+sessionId);
    }

    @Override
    public void onJoined(int channelId, int sessionId){
        ActiveRoom activeGame = activeGameIndex.get(channelId);
        activeGame.join();
        logger.warn("Joined :"+channelId+" : "+sessionId);
    }

    @Override
    public boolean validate(MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        try{
            int sessionId = messageBuffer.readInt();
            long systemId = messageBuffer.readLong();
            long stub = messageBuffer.readLong();
            String ticket = messageBuffer.readUTF8();
            boolean suc = this.validateTicket(systemId,stub,ticket);
            if(suc && sessionId == messageHeader.sessionId){
                ActiveRoom activeGame = activeGameIndex.get(messageHeader.channelId);
                ActiveChannel activeChannel = new ActiveChannel(Long.toString(systemId),stub,activeGame.channelId(),sessionId);
                activeChannel.register(activeGame.gameUserChannel,this.cipherListener);
                gameServiceProvider.onValidated(activeChannel);
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
            channel.addProperty("sessionId",keySync.getAndAdd(this.roomTemplate.capacity()));
            headers[5]="onChannel";
            String resp = httpCaller.post(registerPath,channel.toString().getBytes(),headers);
            JsonObject jo = JsonUtil.parse(resp);
            if(!jo.get("successful").getAsBoolean()) return false;
            ActiveRoom activeRoom =  roomTemplate.assign(channelId);
            activeRoom.gameUserChannel =  new GameUserChannel(channelId, udpEndpointServiceProvider, this.cipherListener,this, this,this,this);
            activeGameIndex.put(channelId, activeRoom);
            udpEndpointServiceProvider.registerUserChannel(activeRoom.gameUserChannel);
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
        gameServiceProvider.onAction(messageHeader,messageBuffer,callback);
    }

    @Override
    public byte[] onRequest(Session session, MessageBuffer.MessageHeader messageHeader, MessageBuffer messageBuffer) {
        ActiveChannel activeChannel = activeChannelIndex.get(messageHeader.sessionId);
        return gameServiceProvider.onRequest(activeChannel.session(),messageHeader,messageBuffer);
    }

    //game context

    public ScheduledFuture<?> schedule(SchedulingTask task) {
        if(task.oneTime()){
            return this.scheduledExecutorService.schedule(task,task.initialDelay()+task.delay(), TimeUnit.MILLISECONDS);
        }else{
            return this.scheduledExecutorService.scheduleAtFixedRate(task,task.initialDelay(),task.delay(),TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onStarted(Room room) {
        this.logger.warn("room started->"+room.channelId());
        this.pendingReleaseRooms.put(room.channelId(),new PendingReleaseRoom(room, LocalDateTime.now().plus(room.duration(), ChronoUnit.MILLIS)));
    }

    @Override
    public void onUpdated(Room room, byte[] payload) {
        this.logger.warn("room updated->"+room.channelId());
        try{
            headers[5]="onUpdate";
            headers[7]= connection.get("configurationName").getAsString();
            String rest = httpCaller.post(registerPath,payload,headers);
            JsonObject suc = JsonUtil.parse(rest);
            if(!suc.get("successful").getAsBoolean()){
                logger.warn("Failed to update game result ["+room.channelId()+"]");
            }
        }catch (Exception ex){
            logger.error("error on updated",ex);
        }
    }

    public void onClosed(Room room){
        logger.warn("room closed->"+room.channelId());
    }

    @Override
    public void onEnded(Room room) {
        this.udpEndpointServiceProvider.releaseUserChannel(room.channelId());
        this.activeGameIndex.remove(room.channelId());
        if(!createChannel()){
            logger.warn("failed to create channel");
        }
    }

    private void onCountdown(){
        pendingReleaseRooms.forEach((k,v)->v.room.onCountdown(gameModuleCountdownInterval));
    }

    private boolean validateTicket(long key,long stub,String ticket){
        try{
           String[] requestHeaders = new String[]{
                    Session.TARANTULA_ACCESS_KEY,
                    headers[1],
                    Session.TARANTULA_SERVER_ID,
                    headers[3],
                    Session.TARANTULA_ACTION,
                    "onTicket"
            };
            JsonObject req = new JsonObject();
            req.addProperty("systemId",key);
            req.addProperty("stub",stub);
            req.addProperty("ticket",ticket);
            JsonObject resp = JsonUtil.parse(httpCaller.post(registerPath,req.toString().getBytes(),requestHeaders));
            return resp.get("successful").getAsBoolean();
        }catch (Exception ex){
            logger.error("failed to validate ticket",ex);
            return false;
        }
    }
    private GameServiceProvider toGameServiceProvider(String className){
        try{
            return (GameServiceProvider)Class.forName(className).getConstructor().newInstance();
        }catch (Exception ex){
            this.logger.warn("No class provided ["+className+"]");
            throw new RuntimeException();
        }
    }
}
