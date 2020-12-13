package com.icodesoftware.integration.udp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.integration.*;
import com.icodesoftware.integration.channel.PushEventChannel;
import com.icodesoftware.integration.server.push.ServerPushMessageHandler;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.HttpCaller;
import com.icodesoftware.util.TarantulaThreadFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class UDPService implements Runnable, GameChannelService, GameChannel.Listener {
    private static TarantulaLogger log = JDKLogger.getLogger(UDPService.class);

    private DatagramSocket datagramChannel;
    private final String address;
    private final int port;
    private final ConcurrentLinkedDeque<PendingMessage> mQueue;
    private final ConcurrentHashMap<Integer, MessageHandler> mHandlers;
    private final ConcurrentHashMap<Long, GameChannelBinding> mChannels;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;
    private final HttpCaller httpCaller;
    private final String configHeader;
    private final JsonObject config;
    private Cipher encrypt;
    private Cipher decrypt;
    private final boolean secured;

    private String serverId;
    private String accessKey;
    private String path;
    private final AtomicInteger sessionId;
    private final AtomicInteger messageId;
    private final AtomicInteger reservedMessageId;
    private final int messageIdOffset;
    private final int schedulingPoolSize;
    private final int poolSize;
    private final long pingTimeout;
    private final long retryTimeout;
    private final long ackTimeout;
    private final int retryCount;
    private final long retryInterval;
    private final String application;
    private final JsonParser parser;
    public UDPService(JsonObject config){
        sessionId = new AtomicInteger(0);
        messageId = new AtomicInteger(1);
        reservedMessageId = new AtomicInteger(Integer.MIN_VALUE);
        parser = new JsonParser();
        this.config = config;
        this.address = config.getAsJsonObject("connection").getAsJsonObject("server").get("binding").getAsString();
        this.port = config.getAsJsonObject("connection").get("port").getAsInt();;
        mQueue = new ConcurrentLinkedDeque<>();
        mHandlers = new ConcurrentHashMap<>();
        mChannels = new ConcurrentHashMap<>();
        configHeader = config.getAsJsonObject("tarantula").get("name").getAsString();
        messageIdOffset = config.getAsJsonObject("tarantula").get("messageIdOffset").getAsInt();
        schedulingPoolSize = config.getAsJsonObject("tarantula").get("schedulingPoolSize").getAsInt();
        poolSize = config.getAsJsonObject("tarantula").get("messagingPoolSize").getAsInt();
        retryCount = config.getAsJsonObject("tarantula").get("retryCount").getAsInt();
        retryInterval = config.getAsJsonObject("tarantula").get("retryInterval").getAsLong();
        pingTimeout = config.getAsJsonObject("tarantula").get("pingTimeout").getAsLong();
        retryTimeout = config.getAsJsonObject("tarantula").get("retryTimeout").getAsLong();
        ackTimeout = config.getAsJsonObject("tarantula").get("ackTimeout").getAsLong();
        application = config.getAsJsonObject("tarantula").get("application").getAsString();
        secured = config.getAsJsonObject("connection").get("secured").getAsBoolean();
        httpCaller = new HttpCaller(config.getAsJsonObject(configHeader).get("url").getAsString());
        AckMessageHandler ackMessageHandler = new AckMessageHandler(this);
        mHandlers.put(ackMessageHandler.type(),ackMessageHandler);
        JoinMessageHandler joinMessageHandler = new JoinMessageHandler(this);
        mHandlers.put(joinMessageHandler.type(),joinMessageHandler);
        MoveMessageHandler moveMessageHandler = new MoveMessageHandler(this);
        mHandlers.put(moveMessageHandler.type(),moveMessageHandler);
        LeaveMessageHandler leaveMessageHandler = new LeaveMessageHandler(this);
        mHandlers.put(leaveMessageHandler.type(),leaveMessageHandler);
        PongMessageHandler pongMessageHandler = new PongMessageHandler(this);
        mHandlers.put(pongMessageHandler.type(),pongMessageHandler);
        SpawnMessageHandler spawnMessageHandler = new SpawnMessageHandler(this);
        mHandlers.put(spawnMessageHandler.type(),spawnMessageHandler);
        DestroyMessageHandler destroyMessageHandler = new DestroyMessageHandler(this);
        mHandlers.put(destroyMessageHandler.type(),destroyMessageHandler);
        CollisionMessageHandler collisionMessageHandler = new CollisionMessageHandler(this);
        mHandlers.put(collisionMessageHandler.type(),collisionMessageHandler);
        SyncMessageHandler syncMessageHandler = new SyncMessageHandler(this);
        mHandlers.put(syncMessageHandler.type(),syncMessageHandler);
        LoadMessageHandler loadMessageHandler = new LoadMessageHandler(this);
        mHandlers.put(loadMessageHandler.type(),loadMessageHandler);
        OnSyncMessageHandler onSyncMessageHandler = new OnSyncMessageHandler(this);
        mHandlers.put(onSyncMessageHandler.type(),onSyncMessageHandler);
        DischargeMessageHandler dischargeMessageHandler = new DischargeMessageHandler(this);
        mHandlers.put(dischargeMessageHandler.type(),dischargeMessageHandler);
        ActionMessageHandler actionMessageHandler = new ActionMessageHandler(this);
        mHandlers.put(actionMessageHandler.type(),actionMessageHandler);
        ServerPushMessageHandler serverPushMessageHandler = new ServerPushMessageHandler(this);
        mHandlers.put(serverPushMessageHandler.type(),serverPushMessageHandler);
    }
    @Override
    public void run(){
        log.warn("WAITING FOR INBOUND MESSAGES ON ["+address+":"+port+"] At ["+serverId+"]");
        while (true){
            try{
                DatagramPacket buffer = new DatagramPacket(new byte[OutboundMessage.MESSAGE_SIZE*2],OutboundMessage.MESSAGE_SIZE*2);
                this.datagramChannel.receive(buffer);
                byte[] payload = Arrays.copyOf(buffer.getData(),buffer.getLength());
                mQueue.offer(new PendingMessage(payload,buffer.getSocketAddress()));
            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
                //try{Thread.sleep(50);}catch (Exception exx){}
            }
        }
    }
    public void start() throws Exception{
        scheduledExecutorService = Executors.newScheduledThreadPool(schedulingPoolSize,new TarantulaThreadFactory("scheduling"));
        this.datagramChannel = new DatagramSocket(null);
        InetSocketAddress addr = new InetSocketAddress(address,port);
        this.datagramChannel.bind(addr);
        //this.datagramChannel.setReceiveBufferSize(OutboundMessage.MESSAGE_SIZE*2);
        //this.datagramChannel.setSendBufferSize(OutboundMessage.MESSAGE_SIZE*2);
        //this.datagramChannel.setSoTimeout(1000);
        executorService = Executors.newFixedThreadPool(poolSize,new TarantulaThreadFactory("messaging"));
        for(int i=0;i<poolSize;i++){
            executorService.execute(()->{
                while (true){
                    try{
                        PendingMessage pendingMessage = mQueue.poll();
                        if(pendingMessage!=null){
                            if(pendingMessage.pendingType == PendingMessage.INBOUND){
                                InboundMessage inboundMessage = new InboundMessage("",secured?ByteBuffer.wrap(decrypt(pendingMessage.data)):ByteBuffer.wrap(pendingMessage.data),pendingMessage.source);
                                GameChannelBinding binding = mChannels.get(inboundMessage.connectionId());
                                if(binding!=null){
                                    binding.gameChannel.onMessage(inboundMessage);
                                }
                            }
                            else if(pendingMessage.pendingType == PendingMessage.OUTBOUND){
                                pendingMessage.runnable.run();
                            }
                        }
                        else{
                            Thread.sleep(5);
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            });
        }
        httpCaller._init();
        this.serverId = UUID.randomUUID().toString();
        this.accessKey = config.getAsJsonObject(configHeader).get("accessKey").getAsString();
        String[] headers = new String[]{
                Session.TARANTULA_ACCESS_KEY,accessKey,
                Session.TARANTULA_ACTION,"onStart",
                Session.TARANTULA_SERVER_ID,serverId
        };
        config.getAsJsonObject("connection").addProperty("serverId",serverId);
        config.getAsJsonObject("connection").getAsJsonObject("server").addProperty("serverId",serverId);
        this.path = config.getAsJsonObject(configHeader).get("path").getAsString();
        int end = reservedMessageId.addAndGet(messageIdOffset);
        JsonObject conn = config.getAsJsonObject("connection");
        conn.addProperty("messageId",end-messageIdOffset);
        conn.addProperty("messageIdOffset",end-1);
        String resp = httpCaller.post(this.path,conn.toString().getBytes(),headers);
        log.warn(resp);
        JsonObject pc = parser.parse(resp).getAsJsonObject();
        if(!pc.get("successful").getAsBoolean()){
            throw new RuntimeException(pc.get("message").getAsString());
        }
        byte[] key = Base64.getDecoder().decode(pc.get("serverKey").getAsString());
        IvParameterSpec iv = new IvParameterSpec(key);
        SecretKey secretKey = new SecretKeySpec(key,DeploymentServiceProvider.SERVER_KEY_SPEC);
        encrypt = Cipher.getInstance(DeploymentServiceProvider.CIPHER_NAME_CBC_PKC5PADDING);
        encrypt.init(Cipher.ENCRYPT_MODE,secretKey,iv);
        decrypt = Cipher.getInstance(DeploymentServiceProvider.CIPHER_NAME_CBC_PKC5PADDING);
        decrypt.init(Cipher.DECRYPT_MODE,secretKey,iv);
        pc.getAsJsonArray("connections").forEach((c)->{
            PushEventChannel _pc = new PushEventChannel(c.getAsLong(),this,retryCount,this.retryInterval);
            _pc.onGame(createGame(_pc));
            _pc.onGame().registerGameChannelListener(this);
            GameChannelBinding binding = new GameChannelBinding(_pc);
            binding.pingSchedule = scheduledExecutorService.scheduleAtFixedRate(()->_pc.ping(),pingTimeout,pingTimeout,TimeUnit.MILLISECONDS);
            binding.retrySchedule = scheduledExecutorService.scheduleAtFixedRate(()->_pc.retry(),retryTimeout,retryTimeout,TimeUnit.MILLISECONDS);
            mChannels.put(_pc.channelId(),binding);
        });
        PushEventChannel pushEventChannel = new PushEventChannel(pc.get("connectionId").getAsLong(),this,retryCount,this.retryInterval);
        pushEventChannel.onGame(createGame(pushEventChannel));
        pushEventChannel.onGame().registerGameChannelListener(this);
        GameChannelBinding mbinding = new GameChannelBinding(pushEventChannel);
        mbinding.pingSchedule = scheduledExecutorService.scheduleAtFixedRate(()->pushEventChannel.ping(),pingTimeout,pingTimeout,TimeUnit.MILLISECONDS);
        mbinding.retrySchedule = scheduledExecutorService.scheduleAtFixedRate(()->pushEventChannel.retry(),retryTimeout,retryTimeout,TimeUnit.MILLISECONDS);
        mChannels.put(pushEventChannel.channelId(),mbinding);

        scheduledExecutorService.scheduleAtFixedRate(()->ack(),ackTimeout,ackTimeout,TimeUnit.MILLISECONDS);
    }
    public void shutdown() throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_ACCESS_KEY,config.getAsJsonObject(configHeader).get("accessKey").getAsString(),
                Session.TARANTULA_ACTION,"onStop",
                Session.TARANTULA_SERVER_ID,config.getAsJsonObject("connection").get("serverId").getAsString()
        };
        httpCaller.get(config.getAsJsonObject(configHeader).get("path").getAsString(),headers);
        this.datagramChannel.close();
    }
    private void ack(){
        try{
            String[] headers = new String[]{
                    Session.TARANTULA_ACCESS_KEY,accessKey,
                    Session.TARANTULA_ACTION,"onAck",
                    Session.TARANTULA_SERVER_ID,serverId
            };
            httpCaller.get(path,headers);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    private long addConnection(){
        try{
            String[] headers = new String[]{
                    Session.TARANTULA_ACCESS_KEY,accessKey,
                    Session.TARANTULA_ACTION,"onConnection",
                    Session.TARANTULA_SERVER_ID,serverId
            };
            JsonObject jsonObject = parser.parse(httpCaller.get(path,headers)).getAsJsonObject();
            return jsonObject.get("connectionId").getAsLong();
        }catch (Exception ex){
            ex.printStackTrace();
            return -1;
        }
    }
    private void update(String zoneId,String roomId,String type,byte[] payload){
        try{
            String[] headers = new String[]{
                    Session.TARANTULA_ACCESS_KEY,accessKey,
                    Session.TARANTULA_ACTION,"onUpdate",
                    Session.TARANTULA_SERVER_ID,serverId,
                    Session.TARANTULA_ROOM_ID,roomId,
                    Session.TARANTULA_ZONE_ID,zoneId,
                    Session.TARANTULA_NAME,type
            };
            httpCaller.post(path,payload,headers);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void pendingOutbound(byte[] pendingMessage,SocketAddress source){
        mQueue.offer(new PendingMessage(()->send(pendingMessage,source)));
    }
    public byte[] encode(OutboundMessage outboundMessage){
        try {
            return secured ? (encrypt(outboundMessage.message())) : (outboundMessage.message());
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
    private void send(byte[] pendingMessage,SocketAddress source){
        try{
            DatagramPacket packet = new DatagramPacket(pendingMessage,pendingMessage.length,source);
            this.datagramChannel.send(packet);
        }catch (Exception ex){

        }
    }
    public boolean validateTicket(int stub,String login,String ticket){
        try{
            String[] headers = new String[]{
                    Session.TARANTULA_TAG,"index/user",
                    Session.TARANTULA_MAGIC_KEY,login,
                    Session.TARANTULA_ACTION,"onTicket"
            };
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("stub",stub);
            jsonObject.addProperty("accessKey",ticket);
            String resp = httpCaller.post("user/action",jsonObject.toString().getBytes(),headers);
            JsonObject ret = parser.parse(resp).getAsJsonObject();
            return ret.get("successful").getAsBoolean();
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
    public GameChannel gameChannel(long connectionId){
        return mChannels.get(connectionId).gameChannel;
    }
    public int sessionId(){
        return sessionId.incrementAndGet();
    }
    public int messageId(){
        return reservedMessageId.incrementAndGet();
    }
    public int[] messageIdRange(){
        int[] mid = new int[2];
        int end = messageId.addAndGet(messageIdOffset);
        mid[0]=end-messageIdOffset;
        mid[1]=end-1;
        return mid;
    }
    public MessageHandler messageHandler(int type){
        return this.mHandlers.get(type);
    }
    private byte[] encrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException{
        synchronized (encrypt){
            return encrypt.doFinal(data);
        }
    }
    private byte[] decrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException{
        synchronized (decrypt){
            return decrypt.doFinal(data);
        }
    }
    private Game createGame(GameChannel gameChannel){
        try {
            return (Game)Class.forName(application).getConstructor(GameChannelService.class,GameChannel.class).newInstance(this,gameChannel);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    @Override
    public void onChannelClosed(GameChannel channelClosed) {
        scheduledExecutorService.schedule(()->{
            log.warn("game channel removed->"+channelClosed.channelId());
            channelClosed.clear();
            GameChannelBinding removed = mChannels.remove(channelClosed.channelId());
            removed.retrySchedule.cancel(true);
            removed.pingSchedule.cancel(true);
        },ackTimeout,TimeUnit.MILLISECONDS);

        long cid = addConnection();
        GameChannel gc = new PushEventChannel(cid,this,retryCount,this.retryInterval);
        gc.onGame(createGame(gc));
        gc.onGame().registerGameChannelListener(this);
        GameChannelBinding binding = new GameChannelBinding(gc);
        binding.pingSchedule = scheduledExecutorService.scheduleAtFixedRate(()->gc.ping(),pingTimeout,pingTimeout,TimeUnit.MILLISECONDS);
        binding.retrySchedule = scheduledExecutorService.scheduleAtFixedRate(()->gc.retry(),retryTimeout,retryTimeout,TimeUnit.MILLISECONDS);
        mChannels.put(gc.channelId(),binding);
    }
    public void onUpdate(Game game,String type,byte[] payload){
        mQueue.offer(new PendingMessage(()->{
            update(game.zoneId(),game.roomId(),type,payload);
        }));
    }
}
