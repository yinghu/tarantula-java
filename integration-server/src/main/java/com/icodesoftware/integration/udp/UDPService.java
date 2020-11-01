package com.icodesoftware.integration.udp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.integration.*;
import com.icodesoftware.integration.channel.PushEventChannel;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.*;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.util.HttpCaller;

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


public class UDPService implements Runnable, GameChannelService {
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
    private int gameChannels;
    private AtomicInteger sessionId;
    private AtomicInteger messageId;
    private AtomicInteger reservedMessageId;
    private int messageIdOffset = 100000;
    private JsonParser parser;
    public UDPService(JsonObject config){
        sessionId = new AtomicInteger(0);
        messageId = new AtomicInteger(1);
        reservedMessageId = new AtomicInteger(Integer.MIN_VALUE);
        parser = new JsonParser();
        this.config = config;
        this.address = config.getAsJsonObject("connection").get("host").getAsString();
        this.port = config.getAsJsonObject("connection").get("port").getAsInt();;
        mQueue = new ConcurrentLinkedDeque<>();
        mHandlers = new ConcurrentHashMap<>();
        mChannels = new ConcurrentHashMap<>();
        configHeader = config.get("tarantula").getAsString();
        secured = config.getAsJsonObject("connection").get("secured").getAsBoolean();
        gameChannels = config.getAsJsonObject(configHeader).get("channels").getAsInt();
        httpCaller = new HttpCaller(config.getAsJsonObject(configHeader).get("url").getAsString());
        AckMessageHandler ackMessageHandler = new AckMessageHandler(this);
        mHandlers.put(ackMessageHandler.type(),ackMessageHandler);
        JoinMessageHandler joinMessageHandler = new JoinMessageHandler(this);
        mHandlers.put(joinMessageHandler.type(),joinMessageHandler);
        EchoMessageHandler echoMessageHandler = new EchoMessageHandler(this);
        mHandlers.put(echoMessageHandler.type(),echoMessageHandler);
        RelayMessageHandler relayMessageHandler = new RelayMessageHandler(this);
        mHandlers.put(relayMessageHandler.type(),relayMessageHandler);
        LeaveMessageHandler leaveMessageHandler = new LeaveMessageHandler(this);
        mHandlers.put(leaveMessageHandler.type(),leaveMessageHandler);
        PongMessageHandler pongMessageHandler = new PongMessageHandler(this);
        mHandlers.put(pongMessageHandler.type(),pongMessageHandler);
        SpawnMessageHandler spawnMessageHandler = new SpawnMessageHandler(this);
        mHandlers.put(spawnMessageHandler.type(),spawnMessageHandler);
        SyncMessageHandler syncMessageHandler = new SyncMessageHandler(this);
        mHandlers.put(syncMessageHandler.type(),syncMessageHandler);
    }
    @Override
    public void run(){
        log.warn("WAITING FOR MESSAGE ON CHANNELS ["+gameChannels+"]");
        while (true){
            try{
                ByteBuffer buffer = ByteBuffer.allocate(OutboundMessage.MESSAGE_SIZE*2);
                SocketAddress src = this.datagramChannel.receive(buffer);
                mQueue.offer(new PendingMessage(buffer,src,PendingMessage.INBOUND));
            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
            }
        }
    }
    public void start() throws Exception{
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        this.datagramChannel = DatagramChannel.open();
        InetSocketAddress iAdd = new InetSocketAddress(address,port);
        this.datagramChannel.bind(iAdd);
        executorService = Executors.newFixedThreadPool(3);
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
                            //send outbound message
                            send(pendingMessage.data,pendingMessage.source);
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
        httpCaller._init();
        String serverId = UUID.randomUUID().toString();
        String[] headers = new String[]{
                Session.TARANTULA_ACCESS_KEY,config.getAsJsonObject(configHeader).get("accessKey").getAsString(),
                Session.TARANTULA_ACTION,"onStart",
                Session.TARANTULA_SERVER_ID,serverId
        };
        config.getAsJsonObject("connection").addProperty("serverId",serverId);
        config.getAsJsonObject("connection").getAsJsonObject("server").addProperty("serverId",serverId);
        String resp = httpCaller.post(config.getAsJsonObject(configHeader).get("path").getAsString(),config.getAsJsonObject("connection").toString().getBytes(),headers);
        log.warn("RESP->"+resp);
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
        PushEventChannel pushEventChannel = new PushEventChannel(pc.get("connectionId").getAsLong(),this);
        mChannels.put(pushEventChannel.channelId(),pushEventChannel);
        mChannels.forEach((k,v)->{
            scheduledExecutorService.scheduleAtFixedRate(()->v.ping(),1000,1000,TimeUnit.MILLISECONDS);
            scheduledExecutorService.scheduleAtFixedRate(()->v.retry(),1000,250,TimeUnit.MILLISECONDS);
        });
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

    public void pendingOutbound(ByteBuffer pendingMessage,SocketAddress source){
        mQueue.offer(new PendingMessage(pendingMessage,source,PendingMessage.OUTBOUND));
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
}
