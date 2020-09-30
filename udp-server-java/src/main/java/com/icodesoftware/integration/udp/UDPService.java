package com.icodesoftware.integration.udp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icodesoftware.protocol.PendingInboundMessage;
import com.icodesoftware.protocol.PendingOutboundMessage;
import com.icodesoftware.service.Serviceable;
import com.icodesoftware.util.HttpCaller;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class UDPService implements Runnable, Serviceable {
    private static Logger log = Logger.getLogger(UDPService.class.getName());

    private DatagramChannel datagramChannel;
    private final String address;
    private final int port;
    private final ConcurrentLinkedDeque<PendingInboundMessage> mQueue;
    private ExecutorService executorService;
    private HttpCaller httpCaller;
    private String configHeader;
    private JsonObject config;
    public UDPService(JsonObject config){
        this.config = config;
        this.address = config.getAsJsonObject("connection").get("host").getAsString();
        this.port = config.getAsJsonObject("connection").get("port").getAsInt();;
        mQueue = new ConcurrentLinkedDeque<>();
        configHeader = config.get("tarantula").getAsString();
        httpCaller = new HttpCaller(config.getAsJsonObject(configHeader).get("url").getAsString());
    }
    @Override
    public void run(){
        System.out.println("WAITING FOR MESSAGE ...");
        while (true){
            try{
                ByteBuffer buffer = ByteBuffer.allocate(PendingOutboundMessage.MESSAGE_SIZE);
                SocketAddress src = this.datagramChannel.receive(buffer);
                PendingInboundMessage inboundMessage = new PendingInboundMessage("",buffer,src);
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
                        log.warning(new String(pendingInboundMessage.payload()));
                    }
                    else{
                        Thread.sleep(100);
                    }
                }catch (Exception ex){

                }
            }
        });
        httpCaller._init();
        String serverId = UUID.randomUUID().toString();
        String[] headers = new String[]{
                HttpCaller.TARANTULA_ACCESS_KEY,config.getAsJsonObject(configHeader).get("accessKey").getAsString(),
                HttpCaller.TARANTULA_ACTION,"onStart",
                HttpCaller.TARANTULA_SERVER_ID,serverId
        };
        config.getAsJsonObject("connection").addProperty("serverId",serverId);
        config.getAsJsonObject("connection").getAsJsonObject("server").addProperty("serverId",serverId);
        JsonParser parser = new JsonParser();
        String resp = httpCaller.post(config.getAsJsonObject(configHeader).get("path").getAsString(),config.getAsJsonObject("connection").toString().getBytes(),headers);
        JsonObject pc = parser.parse(resp).getAsJsonObject();
        if(!pc.get("successful").getAsBoolean()){
            throw new RuntimeException(pc.get("message").getAsString());
        }
        //httpCaller.post()

    }
    public void shutdown() throws Exception{
        String[] headers = new String[]{
                HttpCaller.TARANTULA_ACCESS_KEY,config.getAsJsonObject(configHeader).get("accessKey").getAsString(),
                HttpCaller.TARANTULA_ACTION,"onStop",
                HttpCaller.TARANTULA_SERVER_ID,config.getAsJsonObject("connection").get("serverId").getAsString()
        };
        httpCaller.get(config.getAsJsonObject(configHeader).get("path").getAsString(),headers);
        this.datagramChannel.close();
    }
}
