package com.icodesoftware.integration.udp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.Session;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.PendingInboundMessage;
import com.icodesoftware.protocol.PendingOutboundMessage;
import com.icodesoftware.service.DeploymentServiceProvider;
import com.icodesoftware.service.Serviceable;
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
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class UDPService implements Runnable, Serviceable {
    private static TarantulaLogger log = JDKLogger.getLogger(UDPService.class);

    private DatagramChannel datagramChannel;
    private final String address;
    private final int port;
    private final ConcurrentLinkedDeque<PendingInboundMessage> mQueue;
    private ExecutorService executorService;
    private HttpCaller httpCaller;
    private String configHeader;
    private JsonObject config;
    private Cipher encrypt;
    private Cipher decrypt;
    private boolean secured;
    public UDPService(JsonObject config){
        this.config = config;
        this.address = config.getAsJsonObject("connection").get("host").getAsString();
        this.port = config.getAsJsonObject("connection").get("port").getAsInt();;
        mQueue = new ConcurrentLinkedDeque<>();
        configHeader = config.get("tarantula").getAsString();
        secured = config.getAsJsonObject("connection").get("secured").getAsBoolean();
        httpCaller = new HttpCaller(config.getAsJsonObject(configHeader).get("url").getAsString());
    }
    @Override
    public void run(){
        log.warn("WAITING FOR MESSAGE ...");
        while (true){
            try{
                ByteBuffer buffer = ByteBuffer.allocate(PendingOutboundMessage.MESSAGE_SIZE*2);
                SocketAddress src = this.datagramChannel.receive(buffer);
                if(secured){
                    buffer.flip();
                    byte[] data = new byte[buffer.limit()];
                    log.warn("LIMIT->"+data.length);
                    buffer.get(data,0,data.length);
                    ByteBuffer _buffer = ByteBuffer.wrap(decrypt(data));
                    PendingInboundMessage inboundMessage = new PendingInboundMessage("",_buffer,src);
                    mQueue.offer(inboundMessage);
                }
                else{
                    PendingInboundMessage inboundMessage = new PendingInboundMessage("",buffer,src);
                    mQueue.offer(inboundMessage);
                }
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
                        log.warn(new String(pendingInboundMessage.payload()));
                        log.warn("SEQUENCE->"+(pendingInboundMessage.sequence()+"//type->"+pendingInboundMessage.type()));
                        log.warn("ack->"+pendingInboundMessage.ack()+"->mid->"+pendingInboundMessage.messageId()+"->type->"+pendingInboundMessage.type());
                        log.warn("connectionId->"+pendingInboundMessage.connectionId()+"<>"+pendingInboundMessage.source().toString());
                        PendingOutboundMessage outboundMessage = new PendingOutboundMessage();
                        outboundMessage.ack(false);
                        outboundMessage.connectionId(pendingInboundMessage.connectionId());
                        outboundMessage.messageId(13);
                        outboundMessage.type(1);
                        outboundMessage.sequence(4);
                        outboundMessage.payload(pendingInboundMessage.payload());
                        if(secured){
                            this.datagramChannel.send(ByteBuffer.wrap(encrypt(outboundMessage.message())),pendingInboundMessage.source());
                        }else{
                            this.datagramChannel.send(ByteBuffer.wrap(outboundMessage.message()),pendingInboundMessage.source());
                        }
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
        JsonParser parser = new JsonParser();
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
    private byte[] encrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException{
        return encrypt.doFinal(data);
    }
    private byte[] decrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException{
        return decrypt.doFinal(data);
    }
}
