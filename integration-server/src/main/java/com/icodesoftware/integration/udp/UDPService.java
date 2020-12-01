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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class UDPService implements Runnable, GameChannelService, GameChannel.Listener {
    private static TarantulaLogger log = JDKLogger.getLogger(UDPService.class);

    private DatagramChannel datagramChannel;
    private final String address;
    private final int port;
    private final ConcurrentLinkedDeque<PendingMessage> mQueue;
    private final ConcurrentHashMap<Integer, MessageHandler> mHandlers;
    private final ConcurrentHashMap<Long, GameChannel> mChannels;
    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutorService;
    private HttpCaller httpCaller;
    private String configHeader;
    private JsonObject config;
    private Cipher encrypt;
    private Cipher decrypt;
    private boolean secured;

    private String serverId;
    private String accessKey;
    private String path;
    private AtomicInteger sessionId;
    private AtomicInteger messageId;
    private AtomicInteger reservedMessageId;
    private int messageIdOffset;
    private int schedulingPoolSize;
    private int poolSize;
    private JsonParser parser;
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
        secured = config.getAsJsonObject("connection").get("secured").getAsBoolean();
        httpCaller = new HttpCaller(config.getAsJsonObject(configHeader).get("url").getAsString());
        AckMessageHandler ackMessageHandler = new AckMessageHandler(this);
        mHandlers.put(ackMessageHandler.type(),ackMessageHandler);
        JoinMessageHandler joinMessageHandler = new JoinMessageHandler(this);
        mHandlers.put(joinMessageHandler.type(),joinMessageHandler);
        RelayMessageHandler relayMessageHandler = new RelayMessageHandler(this);
        mHandlers.put(relayMessageHandler.type(),relayMessageHandler);
        LeaveMessageHandler leaveMessageHandler = new LeaveMessageHandler(this);
        mHandlers.put(leaveMessageHandler.type(),leaveMessageHandler);
        PongMessageHandler pongMessageHandler = new PongMessageHandler(this);
        mHandlers.put(pongMessageHandler.type(),pongMessageHandler);
        SpawnMessageHandler spawnMessageHandler = new SpawnMessageHandler(this);
        mHandlers.put(spawnMessageHandler.type(),spawnMessageHandler);
        DestroyMessageHandler destroyMessageHandler = new DestroyMessageHandler(this);
        mHandlers.put(destroyMessageHandler.type(),destroyMessageHandler);
        SyncMessageHandler syncMessageHandler = new SyncMessageHandler(this);
        mHandlers.put(syncMessageHandler.type(),syncMessageHandler);
        OnSyncMessageHandler onSyncMessageHandler = new OnSyncMessageHandler(this);
        mHandlers.put(onSyncMessageHandler.type(),onSyncMessageHandler);
        DischargeMessageHandler dischargeMessageHandler = new DischargeMessageHandler(this);
        mHandlers.put(dischargeMessageHandler.type(),dischargeMessageHandler);
        ServerPushMessageHandler serverPushMessageHandler = new ServerPushMessageHandler(this);
        mHandlers.put(serverPushMessageHandler.type(),serverPushMessageHandler);
    }
    @Override
    public void run(){
        log.warn("WAITING FOR INBOUND MESSAGES ON ["+address+":"+port+"] At ["+serverId+"]");
        while (true){
            try{
                ByteBuffer buffer = ByteBuffer.allocate(OutboundMessage.MESSAGE_SIZE*2);
                SocketAddress src = this.datagramChannel.receive(buffer);
                mQueue.offer(new PendingMessage(buffer,src));
            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
            }
        }
    }
    public void start() throws Exception{
        scheduledExecutorService = Executors.newScheduledThreadPool(schedulingPoolSize,new TarantulaThreadFactory("scheduling"));
        this.datagramChannel = DatagramChannel.open();
        InetSocketAddress iAdd = new InetSocketAddress(address,port);
        this.datagramChannel.bind(iAdd);
        executorService = Executors.newFixedThreadPool(poolSize,new TarantulaThreadFactory("messaging"));
        for(int i=0;i<poolSize;i++){
            executorService.execute(()->{
                while (true){
                    try{
                        PendingMessage pendingMessage = mQueue.poll();
                        if(pendingMessage!=null){
                            if(pendingMessage.pendingType == PendingMessage.INBOUND){
                                ByteBuffer buffer = pendingMessage.data;
                                buffer.flip();
                                byte[] data = new byte[buffer.limit()];
                                buffer.get(data,0,data.length);
                                InboundMessage inboundMessage = new InboundMessage("",secured?ByteBuffer.wrap(decrypt(data)):ByteBuffer.wrap(data),pendingMessage.source);
                                GameChannel gameChannel = mChannels.get(inboundMessage.connectionId());
                                gameChannel.onMessage(inboundMessage);
                            }
                            else if(pendingMessage.pendingType == PendingMessage.OUTBOUND){
                                pendingMessage.runnable.run();
                            }
                        }
                        else{
                            Thread.sleep(10);
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
        pc.getAsJsonArray("connections").forEach((c)->{
            PushEventChannel _pc = new PushEventChannel(c.getAsLong(),this);
            _pc.registerListener(this);
            mChannels.put(_pc.channelId(),_pc);
        });
        byte[] key = Base64.getDecoder().decode(pc.get("serverKey").getAsString());
        IvParameterSpec iv = new IvParameterSpec(key);
        SecretKey secretKey = new SecretKeySpec(key,DeploymentServiceProvider.SERVER_KEY_SPEC);
        encrypt = Cipher.getInstance(DeploymentServiceProvider.CIPHER_NAME_CBC_PKC5PADDING);
        encrypt.init(Cipher.ENCRYPT_MODE,secretKey,iv);
        decrypt = Cipher.getInstance(DeploymentServiceProvider.CIPHER_NAME_CBC_PKC5PADDING);
        decrypt.init(Cipher.DECRYPT_MODE,secretKey,iv);
        PushEventChannel pushEventChannel = new PushEventChannel(pc.get("connectionId").getAsLong(),this);
        pushEventChannel.registerListener(this);
        mChannels.put(pushEventChannel.channelId(),pushEventChannel);
        long pingTimeout = config.getAsJsonObject("tarantula").get("pingTimeout").getAsLong();
        long retryTimeout = config.getAsJsonObject("tarantula").get("retryTimeout").getAsLong();
        long ackTimeout = config.getAsJsonObject("tarantula").get("ackTimeout").getAsLong();
        mChannels.forEach((k,v)->{
            scheduledExecutorService.scheduleAtFixedRate(()->v.ping(),pingTimeout,pingTimeout,TimeUnit.MILLISECONDS);
            scheduledExecutorService.scheduleAtFixedRate(()->v.retry(),retryTimeout,retryTimeout,TimeUnit.MILLISECONDS);
        });
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
    private void update(String zoneId,String roomId,byte[] payload){
        try{
            String[] headers = new String[]{
                    Session.TARANTULA_ACCESS_KEY,accessKey,
                    Session.TARANTULA_ACTION,"onUpdate",
                    Session.TARANTULA_SERVER_ID,serverId,
                    Session.TARANTULA_ROOM_ID,roomId,
                    Session.TARANTULA_ZONE_ID,zoneId
            };
            httpCaller.post(path,payload,headers);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void pendingOutbound(ByteBuffer pendingMessage,SocketAddress source){
        mQueue.offer(new PendingMessage(()->{
            send(pendingMessage,source);
        }));
    }
    public byte[] encode(OutboundMessage outboundMessage){
        try {
            return secured ? (encrypt(outboundMessage.message())) : (outboundMessage.message());
        }catch (Exception ex){
            return null;
        }
    }
    private boolean send(ByteBuffer pendingMessage,SocketAddress source){
        try{
            return this.datagramChannel.send(pendingMessage,source)>0;
        }catch (Exception ex){
            return false;
        }
    }
    public boolean validateTicket(byte[] payload){
        try{
            DataBuffer buffer = new DataBuffer(payload);
            int stub = buffer.getInt();
            String login = buffer.getUTF8();
            String ticket = buffer.getUTF8();
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
        return mChannels.get(connectionId);
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

    @Override
    public void onChannelClosed(GameChannel channelClosed) {
        long cid = addConnection();
        GameChannel gc = new PushEventChannel(cid,this);
        gc.registerListener(this);
        mChannels.put(gc.channelId(),gc);
    }
    public void onUpdate(Game game,byte[] payload){
        mQueue.offer(new PendingMessage(()->{
            update(game.zoneId(),game.roomId(),payload);
        }));
    }
}
