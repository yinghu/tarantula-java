package com.icodesoftware.integration.udp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.integration.*;
import com.icodesoftware.integration.channel.PushEventChannel;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.PendingInboundMessage;
import com.icodesoftware.protocol.PendingOutboundMessage;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class UDPService implements Runnable, GameChannelService {
    private static TarantulaLogger log = JDKLogger.getLogger(UDPService.class);

    private DatagramChannel datagramChannel;
    private final String address;
    private final int port;
    private final ConcurrentLinkedDeque<PendingInboundMessage> mQueue;
    private final ConcurrentHashMap<Integer, MessageHandler> mHandlers;
    private final ConcurrentHashMap<Long, GameChannel> mChannels;
    private ExecutorService executorService;
    private HttpCaller httpCaller;
    private String configHeader;
    private JsonObject config;
    private Cipher encrypt;
    private Cipher decrypt;
    private boolean secured;
    private int gameChannels;
    private AtomicInteger sessionId;
    private JsonParser parser;
    public UDPService(JsonObject config){
        sessionId = new AtomicInteger(0);
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
    }
    @Override
    public void run(){
        log.warn("WAITING FOR MESSAGE ..."+gameChannels);
        while (true){
            try{
                ByteBuffer buffer = ByteBuffer.allocate(PendingOutboundMessage.MESSAGE_SIZE*2);
                SocketAddress src = this.datagramChannel.receive(buffer);
                buffer.flip();
                byte[] data = new byte[buffer.limit()];
                buffer.get(data,0,data.length);
                PendingInboundMessage inboundMessage = new PendingInboundMessage("",secured?ByteBuffer.wrap(decrypt(data)):ByteBuffer.wrap(data),src);
                mQueue.offer(inboundMessage);
            }catch (Exception ex){
                //ignore
                ex.printStackTrace();
            }
        }
    }
    public void start() throws Exception{
        this.datagramChannel = DatagramChannel.open();
        InetSocketAddress iAdd = new InetSocketAddress(address,port);
        this.datagramChannel.bind(iAdd);
        executorService = Executors.newFixedThreadPool(3);
        executorService.execute(()->{
            while (true){
                try{
                    PendingInboundMessage pendingInboundMessage = mQueue.poll();
                    if(pendingInboundMessage!=null){
                        GameChannel gameChannel = mChannels.get(pendingInboundMessage.connectionId());
                        gameChannel.onMessage(pendingInboundMessage);
                    }
                    else{
                        Thread.sleep(100);
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
        PushEventChannel pushEventChannel = new PushEventChannel(pc.get("connectionId").getAsLong(),this);
        mChannels.put(pushEventChannel.channelId(),pushEventChannel);
        byte[] key = Base64.getDecoder().decode(pc.get("serverKey").getAsString());
        IvParameterSpec iv = new IvParameterSpec(key);
        SecretKey secretKey = new SecretKeySpec(key,DeploymentServiceProvider.SERVER_KEY_SPEC);
        encrypt = Cipher.getInstance(DeploymentServiceProvider.CIPHER_NAME_CBC_PKC5PADDING);
        encrypt.init(Cipher.ENCRYPT_MODE,secretKey,iv);
        decrypt = Cipher.getInstance(DeploymentServiceProvider.CIPHER_NAME_CBC_PKC5PADDING);
        decrypt.init(Cipher.DECRYPT_MODE,secretKey,iv);
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
    public boolean send(PendingOutboundMessage outboundMessage,SocketAddress source){
        try{
            if(secured){
                this.datagramChannel.send(ByteBuffer.wrap(encrypt(outboundMessage.message())),source);
            }else{
                this.datagramChannel.send(ByteBuffer.wrap(outboundMessage.message()),source);
            }
            return true;
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
    public MessageHandler messageHandler(int type){
        return this.mHandlers.get(type);
    }
    private byte[] encrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException{
        return encrypt.doFinal(data);
    }
    private byte[] decrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException{
        return decrypt.doFinal(data);
    }
}
